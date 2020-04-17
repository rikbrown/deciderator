import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export abstract class CoinService {
  abstract observeCoinState(uncertaintyId: string): Observable<CoinState>;
  abstract updateCoinState(uncertaintyId: string, coinState: CoinState): void;
}

@Injectable()
export class CoinMockService extends CoinService {
  private coinState: BehaviorSubject<CoinState> = new BehaviorSubject({
    interactive: true,
    rotateDelta: {
      x: -7.22,
      y: 4.5125,
    },
    rotationSpeed: 2,
    drag: 0.9, // 1 = non stop
    quaternion: {
      w: 0.6233080949371493,
      x: 0.31838700000619874,
      y: 0.49244158191774645,
      z: 0.5173181085282266,
    }
  });

  observeCoinState(uncertaintyId: string): Observable<CoinState> {
    return this.coinState.asObservable();
  }

  updateCoinState(uncertaintyId: string, coinState: CoinState): void {
    if (coinState === this.coinState.getValue()) {
      return;
    }

    console.log('coinState update', coinState);
    this.coinState.next(coinState);
  }
}

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
