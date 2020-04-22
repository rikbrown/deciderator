package codes.rik.deciderator.server

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.CoinStateMessage
import codes.rik.deciderator.types.Messages.UncertaintyDetailsMessage
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import codes.rik.deciderator.types.activeOption
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant

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

fun flipCoin(msg: Messages.FlipCoinRequest, session: WebSocketSession) {
  val uncertainty = UncertaintyManager.get(msg.uncertaintyId)!!

  // Start flipping
  val startTime = Instant.now()
  CoinManager.flip(msg.uncertaintyId,
    onUpdate = { coinState ->
      // Callback invoked every time flip updates
      DecideratorHandler.getUncertaintySessions(msg.uncertaintyId)
        .forEach { it.sendMessage(CoinStateMessage(msg.uncertaintyId, coinState)) }
    },
    onComplete = { coinFace, waitTime ->
      val result = FlipResult(
        result = coinFace,
        coinStyle = uncertainty.activeOption.coinStyle,
        flippedBy = session.username,
        waitTime = waitTime,
        flipTime = Duration.between(startTime, Instant.now())
      )
      UncertaintyManager.addResult(msg.uncertaintyId, result)
      session.sendMessage(UncertaintyDetailsMessage(UncertaintyManager.get(msg.uncertaintyId)!!))
    })
}
