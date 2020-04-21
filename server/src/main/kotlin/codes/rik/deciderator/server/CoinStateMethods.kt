package codes.rik.deciderator.server

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.CoinStateMessage
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import org.springframework.web.socket.WebSocketSession

fun updateCoinState(msg: UpdateCoinStateRequest, session: WebSocketSession) {
  CoinManager.update(msg.uncertaintyId, msg.coinState)
  DecideratorHandler.sessionUncertainty
    .filterValues { it == msg.uncertaintyId }
    .keys
    .filterNot { it == session.sessionId } // don't notify the caller
    .mapNotNull{ sessionId -> DecideratorHandler.sessions[sessionId] }
    .forEach { it.sendMessage(CoinStateMessage(msg.uncertaintyId, msg.coinState)) }
}
