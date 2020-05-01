package codes.rik.deciderator.server.methods

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.server.SessionManager
import codes.rik.deciderator.server.sendMessage
import codes.rik.deciderator.server.sessionId
import codes.rik.deciderator.server.username
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.OptionName
import codes.rik.deciderator.types.UncertaintyId
import org.springframework.web.socket.WebSocketSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UncertaintyMethods @Inject constructor(
  private val sessionManager: SessionManager,
  private val uncertaintyManager: UncertaintyManager,
  private val coinManager: CoinManager,
) {

  fun createUncertainty(session: WebSocketSession, msg: Messages.CreateUncertaintyRequest) {
    val id = uncertaintyManager.create(
      name = msg.name,
      options = msg.options.map(::OptionName).toSet()
    )
    session.sendMessage(Messages.UncertaintyCreatedMessage(id))
  }

  fun joinUncertainty(session: WebSocketSession, msg: Messages.JoinUncertaintyRequest) {
    val oldUncertaintyId = sessionManager.linkSessionUncertainty(session.sessionId, msg.uncertaintyId)

    // Is new uncertainty even valid?
//    val uncertainty = uncertainties[uncertaintyId]
//    if (uncertainty == null) {
//      session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
//      return
//    }

    // Join this uncertainty
    session.sendMessage(Messages.UncertaintyJoinedMessage(uncertaintyManager.get(msg.uncertaintyId)))
    session.sendMessage(
      Messages.CoinStateMessage(
        msg.uncertaintyId,
        coinManager.get(msg.uncertaintyId)
      )
    )

    // Notify users. If we left an old uncertainty, notify that too.
    announceUncertaintyUsers(msg.uncertaintyId)
    if (oldUncertaintyId != null) announceUncertaintyUsers(oldUncertaintyId)
  }

  fun leaveUncertainty(session: WebSocketSession, msg: Messages.LeaveUncertaintyRequest) {
    sessionManager.unlinkSessionUncertainty(session.sessionId, msg.uncertaintyId)
    announceUncertaintyUsers(msg.uncertaintyId)
  }

  fun getUncertainty(session: WebSocketSession, msg: Messages.GetUncertaintyRequest) {
    session.sendMessage(Messages.UncertaintyDetailsMessage(uncertaintyManager.get(msg.uncertaintyId)))
    session.sendMessage(
      Messages.CoinStateMessage(
        msg.uncertaintyId,
        coinManager.get(msg.uncertaintyId)
      )
    )
  }

  fun nextRound(msg: Messages.NextRoundRequest) {
    uncertaintyManager.nextRound(msg.uncertaintyId)

    val details = uncertaintyManager.get(msg.uncertaintyId)
    sessionManager.getUncertaintySessions(msg.uncertaintyId)
      .forEach { it.sendMessage(Messages.UncertaintyDetailsMessage(details)) }
  }

  fun announceUncertaintyUsers(uncertaintyId: UncertaintyId) {
    val sessions = sessionManager.getUncertaintySessions(uncertaintyId)
    sessions.forEach { session ->
      session.sendMessage(Messages.UncertaintyUsersMessage(uncertaintyId,
        users = sessions
          .map { it.username }
          .sortedBy { it.username }
          .sortedByDescending { it == session.username },
        username = session.username
      )
      )
    }
  }

}
