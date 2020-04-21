import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {DecideratorSocketService} from '../deciderator-socket/deciderator-socket.service';
import {filter, map} from 'rxjs/operators';

@Injectable()
export abstract class CoinService {
  abstract observeCoinState(uncertaintyId: string): Observable<CoinState>;
  abstract updateCoinState(uncertaintyId: string, coinState: CoinState): void;
  abstract updateCoinStyle(uncertaintyId: string, coinStyle: string): void;
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

@Injectable()
export class DecideratorCoinService extends CoinService {
  constructor(private decideratorSocketService: DecideratorSocketService) {
    super();
  }

  observeCoinState(uncertaintyId: string): Observable<CoinState> {
    return this.decideratorSocketService.coinStateMessageSubject
      .pipe(filter(msg => msg.uncertaintyId === uncertaintyId))
      .pipe(map(msg => msg.coinState));
  }

  updateCoinState(uncertaintyId: string, coinState: CoinState): void {
    const request: UpdateCoinStateRequest = { uncertaintyId, coinState };
    this.decideratorSocketService.send('UpdateCoinStateRequest', request);
  }

  updateCoinStyle(uncertaintyId: string, coinStyle: string): void {
    const request: UpdateCoinStyleRequest = { uncertaintyId, coinStyle };
    this.decideratorSocketService.send('UpdateCoinStyleRequest', request);
  }

}
