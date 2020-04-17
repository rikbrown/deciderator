import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {Uncertainty} from './types';
import {single} from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export abstract class UncertaintyService {
  abstract createUncertainty(uncertainty: Uncertainty): Observable<Uncertainty>;
  abstract getUncertainty(id: string): Observable<Uncertainty>;
  abstract nextRound(uncertaintyId: string): void;
}

@Injectable()
export class UncertaintyMockService extends UncertaintyService {
  private readonly DATA: Uncertainty = {
    id: 'WHD',
    name: 'What game should we play next?',
    rules: {
      bestOf: 5,
      finalTwoHeadToHead: true
    },
    options: [
      {
        name: 'EU4',
        eliminated: false,
      },
      {
        name: 'Civ VI',
        eliminated: false,
        active: {
          roundComplete: true,
          coinStyle: 'usa',
          results: {
            heads: [
              {
                coinStyle: 'usa',
                flippedBy: 'Rik',
                waitTime: 7001,
                flipTime: 12576,
              },
              {
                coinStyle: 'germany',
                flippedBy: 'Mark',
                waitTime: 1234,
                flipTime: 17653,
              },
              {
                coinStyle: 'usa',
                flippedBy: 'Mark',
                waitTime: 32452,
                flipTime: 12345,
              }
            ],
            tails: [
              {
                coinStyle: 'usa',
                flippedBy: 'Rik',
                waitTime: 23321,
                flipTime: 17532,
              },
              {
                coinStyle: 'germany',
                flippedBy: 'Mark',
                waitTime: 2345,
                flipTime: 32443,
              },
            ]
          }
        }
      },
      {
        name: 'HOI4',
        eliminated: true,
      },
      {
        name: 'Stellaris',
        eliminated: false,
      },
      {
        name: 'LOTRO',
        eliminated: false,
      },
    ]
  };

  private readonly UNCERTAINTY: BehaviorSubject<Uncertainty> = new BehaviorSubject(this.DATA);

  createUncertainty(uncertainty: Uncertainty): Observable<Uncertainty> {
    if (uncertainty.name === 'test') {
      return this.UNCERTAINTY.asObservable();
    } else {
      return throwError('Something is broken');
    }
  }

  getUncertainty(id: string): Observable<Uncertainty> {
    if (id === 'foo') {
      return this.UNCERTAINTY.asObservable();
    } else {
      return throwError('Uncertainty does not exist');
    }
  }

  nextRound(uncertaintyId: string): void {
    const d2: Uncertainty = JSON.parse(JSON.stringify(this.DATA));
    // d2.name = 'foo';
    d2.options[3].active = d2.options[1].active;
    d2.options[3].active.roundComplete = false;
    d2.options[3].active.results.heads = [];
    d2.options[3].active.results.tails = [];
    d2.options[1].active = null;
    this.UNCERTAINTY.next(d2);
  }

}
