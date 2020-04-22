import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {DecideratorSocketService} from '../deciderator-socket/deciderator-socket.service';
import {filter, map} from 'rxjs/operators';

@Injectable()
export abstract class CoinService {
  abstract observeCoinState(uncertaintyId: string): Observable<CoinState>;
  abstract updateCoinState(uncertaintyId: string, coinState: CoinState): void;
  abstract updateCoinStyle(uncertaintyId: string, coinStyle: string): void;
  abstract flipCoin(uncertaintyId: string): void;
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

  flipCoin(uncertaintyId: string): void {
    const request: FlipCoinRequest = { uncertaintyId };
    this.decideratorSocketService.send('FlipCoinRequest', request);
  }

}
