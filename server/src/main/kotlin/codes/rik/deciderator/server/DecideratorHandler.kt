package codes.rik.deciderator.server

import codes.rik.deciderator.distinctWithPrevious
import codes.rik.deciderator.slidingPairs
import codes.rik.deciderator.server.methods.CoinStateMethods
import codes.rik.deciderator.server.methods.UncertaintyMethods
import codes.rik.deciderator.types.Messages.ActiveSessionsMessage
import codes.rik.deciderator.types.Messages.CreateUncertaintyRequest
import codes.rik.deciderator.types.Messages.DecideratorRequest
import codes.rik.deciderator.types.Messages.FlipCoinRequest
import codes.rik.deciderator.types.Messages.GetUncertaintyRequest
import codes.rik.deciderator.types.Messages.JoinUncertaintyRequest
import codes.rik.deciderator.types.Messages.LeaveUncertaintyRequest
import codes.rik.deciderator.types.Messages.NextRoundRequest
import codes.rik.deciderator.types.Messages.SetUsernameRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import codes.rik.deciderator.types.Username
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Singleton
class DecideratorHandler @Inject constructor(
  private val sessionManager: SessionManager,
  private val coinStateMethods: CoinStateMethods,
  private val uncertaintyMethods: UncertaintyMethods,
) : TextWebSocketHandler() {

  init {
    sessionManager.sessionsSubject
      .distinctWithPrevious()
      .subscribe { announceActiveSessions(it) }
  }

  override fun afterConnectionEstablished(session: WebSocketSession) {
    sessionManager.addSession(session)
  }

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessionManager.removeSession(session)
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    logger.info { "[INCOMING] ${message.payload}" }

    when (val msg = objectMapper.readValue<DecideratorRequest>(message.payload)) {
      is SetUsernameRequest -> setUsername(session, msg)
      is CreateUncertaintyRequest -> uncertaintyMethods.createUncertainty(session, msg)
      is JoinUncertaintyRequest -> uncertaintyMethods.joinUncertainty(session, msg)
      is GetUncertaintyRequest -> uncertaintyMethods.getUncertainty(session, msg)
      is LeaveUncertaintyRequest -> uncertaintyMethods.leaveUncertainty(session, msg)
      is UpdateCoinStateRequest -> coinStateMethods.updateCoinState(msg)
      is UpdateCoinStyleRequest -> coinStateMethods.updateCoinStyle(msg)
      is FlipCoinRequest -> coinStateMethods.flipCoin(msg, session)
      is NextRoundRequest -> uncertaintyMethods.nextRound(msg)
    }
  }

  private fun setUsername(session: WebSocketSession, msg: SetUsernameRequest) {
    sessionManager.setUsername(session, Username(msg.username))
  }

  private fun announceActiveSessions(sessions: Set<WebSocketSession>) {
    sessions.forEach { session ->
      session.sendMessage(
        ActiveSessionsMessage(
          sessionId = session.sessionId,
          onlineSessionIds = sessionManager.sessionIds
        )
      )
    }
  }

}

private val logger = KotlinLogging.logger {}

