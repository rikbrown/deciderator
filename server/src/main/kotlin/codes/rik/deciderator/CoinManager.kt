package codes.rik.deciderator

import codes.rik.deciderator.types.CoinFace
import codes.rik.deciderator.types.CoinFace.HEADS
import codes.rik.deciderator.types.CoinFace.TAILS
import codes.rik.deciderator.types.UncertaintyId
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Flow
import kotlin.random.Random

object CoinManager {
  private val uncertaintyCoin: MutableMap<UncertaintyId, CoinState> = HashMap()
  private val uncertaintyLastFlip: MutableMap<UncertaintyId, Instant> = HashMap()

  fun get(uncertaintyId: UncertaintyId): CoinState {
    return uncertaintyCoin.getOrPut(uncertaintyId) { DEFAULT_COIN_STATE }
  }

  fun update(uncertaintyId: UncertaintyId, coinState: CoinState) {
    uncertaintyCoin[uncertaintyId] = coinState
  }

  fun flip(uncertaintyId: UncertaintyId, onUpdate: (CoinState) -> Unit, onComplete: (CoinFace, Duration) -> Unit) {
    val now = Instant.now()
    val waitTime = Duration.between(uncertaintyLastFlip[uncertaintyId] ?: now, now)
    val coinState = get(uncertaintyId)
      .copy(
        interactive = false,
        drag = 1.0
      )
      .also { update(uncertaintyId, it) }

    Flipper(coinState,
      onUpdate = {
        update(uncertaintyId, it)
        onUpdate(it)
      },
      onComplete = {
        uncertaintyLastFlip[uncertaintyId] = Instant.now();
        onComplete(it, waitTime)
      }).flip()
  }
}

private class Flipper(initialState: CoinState, val onUpdate: (CoinState) -> Unit, val onComplete: (CoinFace) -> Unit) {
  var coinState = initialState

  fun flip() {
    GlobalScope.launch {
      doFlips()
    }
  }

  suspend fun doFlips() {
    onUpdate(coinState)

    val targetFace = if (Random.nextBoolean()) HEADS else TAILS
    val targetQuaternion = if (targetFace == HEADS) {
      Quaternion(
        x = 1.0,
        y = 0.5,
        z = -1.0,
        w = -0.5
      )
    } else {
      Quaternion(
        x = -0.25,
        y = -0.5,
        z = -0.25,
        w = -0.5
      )
    }

    val rotateMax = Random.nextDouble(2.0, 5.0)
    val rotateMin = Random.nextDouble(1.15, 2.0)
    val maxSpeedDelay = Random.nextInt(1000, 3000)
    val minSpeedDelay = Random.nextInt(1000, 3000)

    while (coinState.rotationSpeed < rotateMax) {
      delayThenChangeSpeed(Random.nextDouble(1.03, 1.07))
    }

    delay(maxSpeedDelay.millis)

    while (coinState.rotationSpeed > rotateMin) {
      delayThenChangeSpeed(Random.nextDouble(0.85, 0.95))
    }
    delay(minSpeedDelay.millis)

    coinState = DEFAULT_COIN_STATE.copy(
        rotateDelta = DeltaXY(0.0, 0.0),
        quaternion = targetQuaternion
      )
      .also(onUpdate)
    onComplete(targetFace)

  }

  private suspend fun delayThenChangeSpeed(speedModifier: Double) {
    delay(Random.nextInt(350, 1250).millis)
    coinState = coinState.copy(
        quaternion = null,
        rotationSpeed = coinState.rotationSpeed * speedModifier
      )
      .also(onUpdate)
  }
}


data class CoinState(
  val interactive: Boolean,
  val rotateDelta: DeltaXY,
  val rotationSpeed: Double,
  val drag: Double,
  val quaternion: Quaternion?
)

data class DeltaXY(val x: Double, val y: Double)
data class Quaternion(
  val w: Double,
  val x: Double,
  val y: Double,
  val z: Double
)

private val DEFAULT_COIN_STATE = CoinState(
  interactive = true,
  rotateDelta = DeltaXY(-7.22, 4.5125),
  rotationSpeed = 2.0,
  drag = 0.9,
  quaternion = Quaternion(
    w = 0.6233080949371493,
    x = 0.31838700000619874,
    y = 0.49244158191774645,
    z = 0.5173181085282266,
  )
)

private val Int.millis get() = Duration.ofMillis(this.toLong())
private val Int.seconds get() = Duration.ofSeconds(this.toLong())
