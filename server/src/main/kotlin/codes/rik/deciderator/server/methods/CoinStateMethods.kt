package codes.rik.deciderator.server.methods

import codes.rik.deciderator.CoinStateManager
import codes.rik.deciderator.UncertaintyManager
import codes.rik.deciderator.server.SessionManager
import codes.rik.deciderator.server.username
import codes.rik.deciderator.types.CoinStyle
import codes.rik.deciderator.types.FlipResult
import codes.rik.deciderator.types.Messages
import codes.rik.deciderator.types.Messages.UpdateCoinStateRequest
import codes.rik.deciderator.types.Messages.UpdateCoinStyleRequest
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinStateMethods @Inject constructor(
  private val uncertaintyManager: UncertaintyManager,
  private val coinStateManager: CoinStateManager,
) {

  fun updateCoinStyle(msg: UpdateCoinStyleRequest) {
    uncertaintyManager.updateCoinStyle(msg.uncertaintyId, CoinStyle(msg.coinStyle))
  }

  fun updateCoinState(msg: UpdateCoinStateRequest) {
    coinStateManager.update(msg.uncertaintyId, msg.coinState)
  }

  /**
   * Flips the coin.
   * Only one caller can flip the coin at a time, and that caller is responsible for updating
   * sessions based on the coin flip.
   */
  fun flipCoin(msg: Messages.FlipCoinRequest, session: WebSocketSession) {
    val uncertainty = uncertaintyManager.get(msg.uncertaintyId).value

    // Start flipping
    // If it's already flipping, this returns completed silently.
    coinStateManager.flip(msg.uncertaintyId)
      .subscribe { (coinFace, startTime, waitTime) ->
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
      }
    // FIXME: dispose?
  }
}
