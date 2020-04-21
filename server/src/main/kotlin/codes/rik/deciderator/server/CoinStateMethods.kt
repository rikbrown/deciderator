package codes.rik.deciderator.server

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.CoinStateMessage
import codes.rik.deciderator.types.Messages.UncertaintyDetailsMessage
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import org.springframework.web.socket.WebSocketSession

fun updateCoinStyle(msg: UpdateCoinStyleRequest, session: WebSocketSession) {
  UncertaintyManager.updateCoinStyle(msg.uncertaintyId, msg.coinStyle)

  DecideratorHandler.getUncertaintySessions(msg.uncertaintyId)
    .filterNot { it.sessionId == session.sessionId } // don't notify the caller
    .forEach { it.sendMessage(UncertaintyDetailsMessage(UncertaintyManager.get(msg.uncertaintyId)!!)) } // FIXME
}

fun updateCoinState(msg: UpdateCoinStateRequest, session: WebSocketSession) {
  CoinManager.update(msg.uncertaintyId, msg.coinState)

  DecideratorHandler.getUncertaintySessions(msg.uncertaintyId)
    .filterNot { it.sessionId == session.sessionId } // don't notify the caller
    .forEach { it.sendMessage(CoinStateMessage(msg.uncertaintyId, msg.coinState)) }
}
