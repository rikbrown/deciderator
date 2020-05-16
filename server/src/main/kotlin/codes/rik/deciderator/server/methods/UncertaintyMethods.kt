package codes.rik.deciderator.server.methods

import codes.rik.deciderator.CoinStateManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.slidingPairs
import codes.rik.deciderator.server.SessionManager
import codes.rik.deciderator.server.sendMessage
import codes.rik.deciderator.server.sessionId
import codes.rik.deciderator.server.username
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.OptionName
import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyId
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import mu.KotlinLogging
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UncertaintyMethods @Inject constructor(
  private val sessionManager: SessionManager,
  private val uncertaintyManager: UncertaintyManager,
  private val coinStateManager: CoinStateManager,
) {
  private val uncertaintyDisposables: ConcurrentMap<UncertaintyId, CompositeDisposable> = ConcurrentHashMap()
  private val sessionUncertaintyDisposables: ConcurrentMap<Pair<SessionId, UncertaintyId>, CompositeDisposable> = ConcurrentHashMap()

  // Watch changes in sessions for username changes and notify clients as appropriate
  init {
    sessionManager.sessionsSubject
      .map { sessions -> sessions
        .map { it to sessionManager.getSessionUncertainty(it.sessionId) }
        .groupBy { (_, uncertaintyId) -> uncertaintyId }
        .mapValues { (_, pairs) ->
          pairs.toMap()
            .keys
            .map { it to it.username }
            .toSet()
        }
      }
      .slidingPairs()
      .subscribe { (prevUncertaintySessions, uncertaintySessions) ->
        uncertaintySessions?.forEach { (uncertaintyId, nextSessions) ->
          if (uncertaintyId != null) {
            val prevSessions = prevUncertaintySessions?.get(uncertaintyId) ?: setOf()
            if (prevSessions != nextSessions) {
              val usernames = nextSessions
                .map { (_, username) -> username }
                .sortedBy { it.username }

              (prevSessions + nextSessions)
                .filter { (session, _) -> session.isOpen }
                .forEach { (session, _) ->
                  session.sendMessage(
                    Messages.UncertaintyUsersMessage(
                      uncertaintyId,
                      users = usernames.sortedByDescending { it == session.username },
                      username = session.username
                    )
                  )
                }
            }
          }
        }
      }

  }

  fun createUncertainty(session: WebSocketSession, msg: Messages.CreateUncertaintyRequest) {
    // Create uncertainty
    val id = uncertaintyManager.create(
      name = msg.name,
      options = msg.options.map(::OptionName).toSet()
    )
    session.sendMessage(Messages.UncertaintyCreatedMessage(id))
  }

  fun joinUncertainty(session: WebSocketSession, msg: Messages.JoinUncertaintyRequest) {
    logger.info { "${session.username} joining ${msg.uncertaintyId}" }

    // Create link
    sessionManager.linkSessionUncertainty(session.sessionId, msg.uncertaintyId)

    // Subscribe to uncertainty updates and send details
    val uncertaintySubject = uncertaintyManager.get(msg.uncertaintyId)
    uncertaintySubject
      .subscribe {
        session.sendMessage(Messages.UncertaintyDetailsMessage(it))
      }
      .addTo(getDisposable(msg.uncertaintyId))
      .addTo(sessionManager.getDisposable(session.sessionId))
      .addTo(getSessionDisposables(session.sessionId, msg.uncertaintyId))

    // Subscribe to coin state updates and send details
    coinStateManager.get(msg.uncertaintyId)
      .subscribe {
        session.sendMessage(Messages.CoinStateMessage(msg.uncertaintyId, it))
      }
      .addTo(getDisposable(msg.uncertaintyId))
      .addTo(sessionManager.getDisposable(session.sessionId))
      .addTo(getSessionDisposables(session.sessionId, msg.uncertaintyId))


    // Is new uncertainty even valid?
//    val uncertainty = uncertainties[uncertaintyId]
//    if (uncertainty == null) {
//      session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
//      return
//    }

    // Join this uncertainty
    session.sendMessage(Messages.UncertaintyJoinedMessage(uncertaintySubject.value))

  }

  fun leaveUncertainty(session: WebSocketSession, msg: Messages.LeaveUncertaintyRequest) {
    sessionManager.unlinkSessionUncertainty(session.sessionId, msg.uncertaintyId)
    sessionUncertaintyDisposables.remove(Pair(session.sessionId, msg.uncertaintyId))?.dispose()
  }

  // TODO: when used?
  fun getUncertainty(session: WebSocketSession, msg: Messages.GetUncertaintyRequest) {
    session.sendMessage(Messages.UncertaintyDetailsMessage(uncertaintyManager.get(msg.uncertaintyId).value))
    session.sendMessage( // FIXME
      Messages.CoinStateMessage(
        msg.uncertaintyId,
        coinStateManager.get(msg.uncertaintyId).value
      )
    )
  }

  fun nextRound(msg: Messages.NextRoundRequest) {
    uncertaintyManager.nextRound(msg.uncertaintyId)
  }

  private fun getDisposable(uncertaintyId: UncertaintyId): CompositeDisposable = uncertaintyDisposables.computeIfAbsent(uncertaintyId) { CompositeDisposable() }
  private fun getSessionDisposables(sessionId: SessionId, uncertaintyId: UncertaintyId)
    = sessionUncertaintyDisposables.computeIfAbsent(Pair(sessionId, uncertaintyId)) { CompositeDisposable() }

}

private val logger = KotlinLogging.logger {}
