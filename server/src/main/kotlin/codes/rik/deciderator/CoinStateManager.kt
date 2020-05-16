package codes.rik.deciderator

import codes.rik.deciderator.types.CoinFace
import codes.rik.deciderator.types.CoinFace.HEADS
import codes.rik.deciderator.types.CoinFace.TAILS
import codes.rik.deciderator.types.Uncertainty
import codes.rik.deciderator.types.UncertaintyId
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy.LATEST
import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private data class CoinStates(
  val isNonInteractive: ConcurrentMap<UncertaintyId, Boolean> = ConcurrentHashMap(),
  val uncertaintyCoin: ConcurrentMap<UncertaintyId, CoinState> = ConcurrentHashMap(),
  val uncertaintyLastFlip: MutableMap<UncertaintyId, Instant> = HashMap(),
) {
  private val coinStateSubject: ConcurrentMap<UncertaintyId, BehaviorSubject<CoinState>> = ConcurrentHashMap()

  operator fun get(id: UncertaintyId): CoinState = uncertaintyCoin.getOrDefault(id, DEFAULT_COIN_STATE)
  fun getSubject(uncertaintyId: UncertaintyId): BehaviorSubject<CoinState>
    = coinStateSubject.computeIfAbsent(uncertaintyId) { BehaviorSubject.createDefault(get(uncertaintyId)) }

  fun update(uncertaintyId: UncertaintyId, coinState: CoinState) {
    uncertaintyCoin[uncertaintyId] = coinState
    getSubject(uncertaintyId).onNext(coinState)
  }

  fun setNonInteractive(uncertaintyId: UncertaintyId): Boolean {
    if (isNonInteractive.put(uncertaintyId, true) == true) {
      return false
    }

    update(uncertaintyId, get(uncertaintyId).copy(interactive = false))
    return true
  }
}

/**
 * Manages the state of the coin for uncertainties
 */
@Singleton
class CoinStateManager @Inject constructor() {
  private val coinStates = CoinStates()

  fun get(uncertaintyId: UncertaintyId) = coinStates.getSubject(uncertaintyId)

  fun update(uncertaintyId: UncertaintyId, coinState: CoinState) {
    if (coinStates.isNonInteractive[uncertaintyId] == true) {
      return
    }

    coinStates.update(uncertaintyId, coinState)
  }

  fun flip(uncertaintyId: UncertaintyId): Single<FinalCoinStateResult> {
    // Switch to non-interactive and don't flip if already non-interactive
    if (!coinStates.setNonInteractive(uncertaintyId)) {
      logger.info { "Already flipping for $uncertaintyId, ignoring flip request" }
      return Single.error(RuntimeException("Already flipping")) // FIXME
    }

    val now = Instant.now()
    val waitTime = Duration.between(coinStates.uncertaintyLastFlip[uncertaintyId] ?: now, now)
    return Single.create(Flipper(uncertaintyId, coinStates, waitTime))
  }
}

private class Flipper(
  private val uncertaintyId: UncertaintyId,
  private val coinStates: CoinStates,
  private val waitTime: Duration,
): SingleOnSubscribe<FinalCoinStateResult> {
  var coinState = coinStates[uncertaintyId].copy(drag = 1.0)

  override fun subscribe(emitter: SingleEmitter<FinalCoinStateResult>) {
    GlobalScope.launch {
      doFlips(emitter)
    }
  }

  private suspend fun doFlips(emitter: SingleEmitter<FinalCoinStateResult>) {
    val startTime = Instant.now()

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
      .also { coinStates.update(uncertaintyId, it) }

    emitter.onSuccess(FinalCoinStateResult(targetFace,
      waitTime = waitTime,
    startTime = startTime))
  }

  private suspend fun delayThenChangeSpeed(speedModifier: Double) {
    delay(Random.nextInt(350, 1250).millis)
    coinState = coinState.copy(
        quaternion = null,
        rotationSpeed = coinState.rotationSpeed * speedModifier
      )
      .also { coinStates.update(uncertaintyId, it) }
  }
}

data class FinalCoinStateResult(
  val targetFace: CoinFace,
  val startTime: Instant,
  val waitTime: Duration,
)

data class CoinState(
  val interactive: Boolean,
  val rotateDelta: DeltaXY,
  val rotationSpeed: Double,
  val drag: Double,
  val quaternion: Quaternion?,
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

private val logger = KotlinLogging.logger {}
