package codes.rik.deciderator

import codes.rik.deciderator.FlipInfo.CoinState
import codes.rik.deciderator.FlipInfo.TargetFace
import codes.rik.deciderator.types.CoinFace
import codes.rik.deciderator.types.CoinFace.HEADS
import codes.rik.deciderator.types.CoinFace.TAILS
import codes.rik.deciderator.types.UncertaintyId
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.BackpressureStrategy.LATEST
import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CoinManager @Inject constructor() {
  private val uncertaintyCoin: MutableMap<UncertaintyId, CoinState> = HashMap()
  private val uncertaintyLastFlip: MutableMap<UncertaintyId, Instant> = HashMap()

  fun get(uncertaintyId: UncertaintyId): CoinState {
    return uncertaintyCoin.getOrPut(uncertaintyId) { DEFAULT_COIN_STATE }
  }

  fun update(uncertaintyId: UncertaintyId, coinState: CoinState) {
    uncertaintyCoin[uncertaintyId] = coinState
  }

  fun flip(uncertaintyId: UncertaintyId): Pair<Flowable<FlipInfo>, Duration> {
    val now = Instant.now()
    val waitTime = Duration.between(uncertaintyLastFlip[uncertaintyId] ?: now, now)
    val coinState = get(uncertaintyId)
      .copy(
        interactive = false,
        drag = 1.0
      )
      .also { update(uncertaintyId, it) }

    return Flowable.create(Flipper(coinState), LATEST)
      .doOnNext { if (it is CoinState) { update(uncertaintyId, it) } }
      .doOnComplete { uncertaintyLastFlip[uncertaintyId] = Instant.now() } to waitTime
  }
}

private class Flipper(initialState: CoinState): FlowableOnSubscribe<FlipInfo> {
  var coinState = initialState

  override fun subscribe(emitter: FlowableEmitter<FlipInfo>) {
    GlobalScope.launch {
      doFlips(emitter)
    }
  }

  private suspend fun doFlips(subscriber: Emitter<FlipInfo>) {
    subscriber.onNext(coinState)

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

    val rotateMax = Random.nextDouble(2.0, 6.0)
    val rotateMin = Random.nextDouble(1.15, 2.0)
    val maxSpeedDelay = Random.nextInt(500, 4000)
    val minSpeedDelay = Random.nextInt(500, 4000)

    while (coinState.rotationSpeed < rotateMax) {
      delayThenChangeSpeed(subscriber, Random.nextDouble(1.03, 1.07))
    }

    delay(maxSpeedDelay.millis)

    while (coinState.rotationSpeed > rotateMin) {
      delayThenChangeSpeed(subscriber, Random.nextDouble(0.85, 0.95))
    }
    delay(minSpeedDelay.millis)

    coinState = DEFAULT_COIN_STATE.copy(
        rotateDelta = DeltaXY(0.0, 0.0),
        quaternion = targetQuaternion
      )
      .also { subscriber.onNext(it) }

    subscriber.onNext(TargetFace(targetFace))
    subscriber.onComplete()
  }

  private suspend fun delayThenChangeSpeed(subscriber: Emitter<FlipInfo>, speedModifier: Double) {
    delay(Random.nextInt(350, 1250).millis)
    coinState = coinState.copy(
        quaternion = null,
        rotationSpeed = coinState.rotationSpeed * speedModifier
      )
      .also { subscriber.onNext(it) }
  }


}

sealed class FlipInfo {
  data class TargetFace(val targetFace: CoinFace) : FlipInfo()
  data class CoinState(
    val interactive: Boolean,
    val rotateDelta: DeltaXY,
    val rotationSpeed: Double,
    val drag: Double,
    val quaternion: Quaternion?
  ) : FlipInfo()
}


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
