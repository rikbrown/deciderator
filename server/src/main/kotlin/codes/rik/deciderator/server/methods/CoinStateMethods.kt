package codes.rik.deciderator.server.methods

import codes.rik.deciderator.CoinManager
import codes.rik.deciderator.FlipInfo.CoinState
import codes.rik.deciderator.FlipInfo.TargetFace
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.server.SessionManager
import codes.rik.deciderator.server.sendMessage
import codes.rik.deciderator.server.sessionId
import codes.rik.deciderator.server.username
import codes.rik.deciderator.types.CoinStyle
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.CoinStateMessage
import codes.rik.deciderator.types.Messages.UncertaintyDetailsMessage
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import io.reactivex.rxjava3.kotlin.ofType
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinStateMethods @Inject constructor(
  private val sessionManager: SessionManager,
  private val uncertaintyManager: UncertaintyManager,
  private val coinManager: CoinManager,
) {

  fun updateCoinStyle(msg: UpdateCoinStyleRequest, session: WebSocketSession) {
    uncertaintyManager.updateCoinStyle(msg.uncertaintyId, CoinStyle(msg.coinStyle))

    val detailsMsg = UncertaintyDetailsMessage(uncertaintyManager.get(msg.uncertaintyId))
    sessionManager.getUncertaintySessions(msg.uncertaintyId)
      .filterNot { it.sessionId == session.sessionId } // don't notify the caller
      .forEach { it.sendMessage(detailsMsg) }
  }

  fun updateCoinState(msg: UpdateCoinStateRequest, session: WebSocketSession) {
    coinManager.update(msg.uncertaintyId, msg.coinState)

    val coinStateMsg = CoinStateMessage(msg.uncertaintyId, msg.coinState)
    sessionManager.getUncertaintySessions(msg.uncertaintyId)
      .filterNot { it.sessionId == session.sessionId } // don't notify the caller
      .forEach { it.sendMessage(coinStateMsg) }
  }

  fun flipCoin(msg: Messages.FlipCoinRequest, session: WebSocketSession) {
    val uncertainty = uncertaintyManager.get(msg.uncertaintyId)

    // Start flipping
    val startTime = Instant.now()

    val (flowable, waitTime) = coinManager.flip(msg.uncertaintyId)
    flowable.publish()
      .apply {
        ofType<CoinState>()
          .subscribe { coinState ->
            // Callback invoked every time flip updates - send the new rotation state
            sessionManager.getUncertaintySessions(msg.uncertaintyId)
              .forEach { it.sendMessage(CoinStateMessage(msg.uncertaintyId, coinState)) }
          }
      }
      .apply {
        ofType<TargetFace>()
          .map { it.targetFace }
          .firstElement()
          .subscribe { coinFace ->
            // When flipping is complete, convert it into a result, add it, and send the latest details
            val result = FlipResult(
              result = coinFace,
              coinStyle = uncertainty.currentRound.coinStyle,
              flippedBy = session.username,
              waitTime = waitTime,
              flipTime = Duration.between(startTime, Instant.now())
            )

            uncertaintyManager.addResult(
              msg.uncertaintyId,
              result
            ) // This may trigger elimination/round end

            val detailsMsg = UncertaintyDetailsMessage(uncertaintyManager.get(msg.uncertaintyId))
            sessionManager.getUncertaintySessions(msg.uncertaintyId)
              .forEach { it.sendMessage(detailsMsg) }
          }
      }
      .connect()
  }
}
