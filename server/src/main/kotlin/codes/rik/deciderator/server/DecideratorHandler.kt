package codes.rik.deciderator.server

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.types.Messages
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
import codes.rik.deciderator.types.SessionId
import codes.rik.deciderator.types.UncertaintyId
import codes.rik.deciderator.types.Username
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object DecideratorHandler : TextWebSocketHandler() {
  val sessions: ConcurrentMap<SessionId, WebSocketSession> = ConcurrentHashMap()
  val sessionUncertainty: ConcurrentMap<SessionId, UncertaintyId> = ConcurrentHashMap()

  override fun afterConnectionEstablished(session: WebSocketSession) {
    sessions[session.sessionId] = session
    announceActiveSessions()
  }

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessions.remove(session.sessionId)
    announceActiveSessions()
    sessionUncertainty[session.sessionId]?.let { announceUncertaintyUsers(it) }
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    println(message.payload)
    when (val msg = objectMapper.readValue<DecideratorRequest>(message.payload)) {
      is CreateUncertaintyRequest -> createUncertainty(session, msg)
      is JoinUncertaintyRequest -> joinUncertainty(session, msg)
      is GetUncertaintyRequest -> getUncertainty(session, msg)
      is SetUsernameRequest -> setUsername(session, msg)
      is LeaveUncertaintyRequest -> leaveUncertainty(session, msg)
      is UpdateCoinStateRequest -> updateCoinState(msg, session)
      is UpdateCoinStyleRequest -> updateCoinStyle(msg, session)
      is FlipCoinRequest -> flipCoin(msg, session)
      is NextRoundRequest -> nextRound(session, msg)
    }
  }

  fun getUncertaintySessions(uncertaintyId: UncertaintyId): Set<WebSocketSession> {
    return sessionUncertainty
      .filterValues { it == uncertaintyId }
      .keys
      .mapNotNull { sessions[it] }
      .toSet()
  }

  private fun setUsername(session: WebSocketSession, msg: SetUsernameRequest) {
    session.username = Username(msg.username);
    sessionUncertainty[session.sessionId]?.let { announceUncertaintyUsers(it) }
  }

  private fun createUncertainty(session: WebSocketSession, msg: CreateUncertaintyRequest) {
    val id = UncertaintyManager.create(
      name = msg.name,
      options = msg.options
    )
    session.sendMessage(UncertaintyCreatedMessage(id))
  }

  private fun joinUncertainty(session: WebSocketSession, msg: JoinUncertaintyRequest) {
    val oldUncertaintyId = sessionUncertainty[session.sessionId]

    // Is new uncertainty even valid?
//    val uncertainty = uncertainties[uncertaintyId]
//    if (uncertainty == null) {
//      session.sendMessage(UncertaintyNotFoundMessage(uncertaintyId))
//      return
//    }

    // Join this uncertainty
//    session.username = Username(msg.username)
    sessionUncertainty[session.sessionId] = msg.uncertaintyId
    session.sendMessage(UncertaintyJoinedMessage(UncertaintyManager.get(msg.uncertaintyId)))
    session.sendMessage(CoinStateMessage(msg.uncertaintyId, CoinManager.get(msg.uncertaintyId)))

    // Notify users. If we left an old uncertainty, notify that too.
    announceUncertaintyUsers(msg.uncertaintyId)
    if (oldUncertaintyId != null) announceUncertaintyUsers(oldUncertaintyId)
  }

  private fun leaveUncertainty(session: WebSocketSession, msg: LeaveUncertaintyRequest) {
    sessionUncertainty[session.sessionId]
      ?.takeIf { it == msg.uncertaintyId }
      ?.let { uncertaintyId ->
        sessionUncertainty.remove(session.sessionId)
        announceUncertaintyUsers(uncertaintyId)
      }
  }

  private fun getUncertainty(session: WebSocketSession, msg: GetUncertaintyRequest) {
    val uncertainty = UncertaintyManager.get(msg.uncertaintyId)
    session.sendMessage(UncertaintyDetailsMessage(uncertainty))
    session.sendMessage(CoinStateMessage(msg.uncertaintyId, CoinManager.get(msg.uncertaintyId)))
//    announceUncertaintyUsers(msg.uncertaintyId) // also announce users
  }

  private fun nextRound(session: WebSocketSession, msg: NextRoundRequest) {
    UncertaintyManager.nextRound(msg.uncertaintyId)
    getUncertaintySessions(msg.uncertaintyId)
      .forEach { it.sendMessage(UncertaintyDetailsMessage(UncertaintyManager.get(msg.uncertaintyId))) }
  }

  private fun announceActiveSessions() {
    sessions.forEach { (sessionId, session) ->
      session.sendMessage(
        ActiveSessionsMessage(
          sessionId = sessionId,
          onlineSessionIds = sessions.keys
        )
      )
    }
  }

  private fun announceUncertaintyUsers(uncertaintyId: UncertaintyId) {
    val sessions = sessionUncertainty
      .filter { (_, sessionUncertaintyId) -> sessionUncertaintyId == uncertaintyId}
      .mapNotNull { (sessionId, _) -> sessions[sessionId] }

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


