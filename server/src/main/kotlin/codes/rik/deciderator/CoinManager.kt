package codes.rik.deciderator

import codes.rik.deciderator.types.UncertaintyId
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.concurrent.Flow

object CoinManager {
  private val uncertaintyCoin: MutableMap<UncertaintyId, CoinState> = HashMap()
  val coinSubject: Subject<CoinState> = PublishSubject.create();

  fun get(uncertaintyId: UncertaintyId): CoinState {
    return uncertaintyCoin.getOrPut(uncertaintyId, {
      CoinState(
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
    })
  }

  fun update(uncertaintyId: UncertaintyId, coinState: CoinState) {
    uncertaintyCoin[uncertaintyId] = coinState
//    coinSubject.onNext(coinState);
  }
}

data class CoinState(
  val interactive: Boolean,
  val rotateDelta: DeltaXY,
  val rotationSpeed: Double,
  val drag: Double,
  val quaternion: Quaternion
)

data class DeltaXY(val x: Double, val y: Double)
data class Quaternion(
  val w: Double,
  val x: Double,
  val y: Double,
  val z: Double
)

/*
export interface CoinState {
  interactive: boolean;
  rotateDelta?: {
    x: number,
    y: number,
  };
  rotationSpeed: number;
  drag: number;
  quaternion?: {
    w: number,
    x: number,
    y: number,
    z: number,
  };
}

 */
