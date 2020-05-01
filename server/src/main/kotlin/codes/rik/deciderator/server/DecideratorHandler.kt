package codes.rik.deciderator.server

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.types.Messages.ActiveSessionsMessage
import codes.rik.deciderator.types.Messages.CoinStateMessage
import codes.rik.deciderator.types.Messages.CreateUncertaintyRequest
import codes.rik.deciderator.types.Messages.GetUncertaintyRequest
import codes.rik.deciderator.types.Messages.JoinUncertaintyRequest
import codes.rik.deciderator.types.Messages.LeaveUncertaintyRequest
import codes.rik.deciderator.types.Messages.SetUsernameRequest
import codes.rik.deciderator.types.Messages.UncertaintyCreatedMessage
import codes.rik.deciderator.types.Messages.UncertaintyDetailsMessage
import codes.rik.deciderator.types.Messages.UncertaintyJoinedMessage
import codes.rik.deciderator.types.Messages.DecideratorRequest
import codes.rik.deciderator.types.Messages.FlipCoinRequest
import codes.rik.deciderator.types.Messages.NextRoundRequest
import codes.rik.deciderator.types.Messages.UncertaintyUsersMessage
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import codes.rik.deciderator.types.OptionName
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.Username
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DecideratorHandler @Inject constructor(
  private val sessionManager: SessionManager,
  private val uncertaintyManager: UncertaintyManager,
  private val coinManager: CoinManager,
  private val coinStateMethods: CoinStateMethods,
) : TextWebSocketHandler() {

  override fun afterConnectionEstablished(session: WebSocketSession) {
    sessionManager.addSession(session)
    announceActiveSessions()
  }

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessionManager.removeSession(session)
    announceActiveSessions()
    sessionManager.getSessionUncertainty(session.sessionId)?.let { announceUncertaintyUsers(it) }
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    println(message.payload)
    when (val msg = objectMapper.readValue<DecideratorRequest>(message.payload)) {
      is CreateUncertaintyRequest -> createUncertainty(session, msg)
      is JoinUncertaintyRequest -> joinUncertainty(session, msg)
      is GetUncertaintyRequest -> getUncertainty(session, msg)
      is SetUsernameRequest -> setUsername(session, msg)
      is LeaveUncertaintyRequest -> leaveUncertainty(session, msg)
      is UpdateCoinStateRequest -> coinStateMethods.updateCoinState(msg, session)
      is UpdateCoinStyleRequest -> coinStateMethods.updateCoinStyle(msg, session)
      is FlipCoinRequest -> coinStateMethods.flipCoin(msg, session)
      is NextRoundRequest -> nextRound(msg)
    }
  }

  private fun setUsername(session: WebSocketSession, msg: SetUsernameRequest) {
    session.username = Username(msg.username);
    sessionManager.getSessionUncertainty(session.sessionId)?.let { announceUncertaintyUsers(it) }
  }

  private fun createUncertainty(session: WebSocketSession, msg: CreateUncertaintyRequest) {
    val id = uncertaintyManager.create(
      name = msg.name,
      options = msg.options.map(::OptionName).toSet()
    )
    session.sendMessage(UncertaintyCreatedMessage(id))
  }

  private fun joinUncertainty(session: WebSocketSession, msg: JoinUncertaintyRequest) {
    val oldUncertaintyId = sessionManager.linkSessionUncertainty(session.sessionId, msg.uncertaintyId)

    // Is new uncertainty even valid?
//    val uncertainty = uncertainties[uncertaintyId]
//    if (uncertainty == null) {
//      session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
//      return
//    }

    // Join this uncertainty
    session.sendMessage(UncertaintyJoinedMessage(uncertaintyManager.get(msg.uncertaintyId)))
    session.sendMessage(CoinStateMessage(msg.uncertaintyId, coinManager.get(msg.uncertaintyId)))

    // Notify users. If we left an old uncertainty, notify that too.
    announceUncertaintyUsers(msg.uncertaintyId)
    if (oldUncertaintyId != null) announceUncertaintyUsers(oldUncertaintyId)
  }

  private fun leaveUncertainty(session: WebSocketSession, msg: LeaveUncertaintyRequest) {
    sessionManager.unlinkSessionUncertainty(session.sessionId, msg.uncertaintyId)
    announceUncertaintyUsers(msg.uncertaintyId)
  }

  private fun getUncertainty(session: WebSocketSession, msg: GetUncertaintyRequest) {
    session.sendMessage(UncertaintyDetailsMessage(uncertaintyManager.get(msg.uncertaintyId)))
    session.sendMessage(CoinStateMessage(msg.uncertaintyId, coinManager.get(msg.uncertaintyId)))
  }

  private fun nextRound(msg: NextRoundRequest) {
    uncertaintyManager.nextRound(msg.uncertaintyId)

    val details = uncertaintyManager.get(msg.uncertaintyId)
    sessionManager.getUncertaintySessions(msg.uncertaintyId)
      .forEach { it.sendMessage(UncertaintyDetailsMessage(details)) }
  }

  private fun announceActiveSessions() {
    sessionManager.forEach { sessionId, session ->
      session.sendMessage(
        ActiveSessionsMessage(
          sessionId = sessionId,
          onlineSessionIds = sessionManager.sessionIds
        )
      )
    }
  }

  private fun announceUncertaintyUsers(uncertaintyId: UncertaintyId) {
    val sessions = sessionManager.getUncertaintySessions(uncertaintyId)
    sessions.forEach { session ->
      session.sendMessage(UncertaintyUsersMessage(uncertaintyId,
        users = sessions
          .map { it.username }
          .sortedBy { it.username }
          .sortedByDescending { it == session.username },
        username = session.username))
    }

  }
}


