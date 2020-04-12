import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {Uncertainty} from './types';
import {single} from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export abstract class UncertaintyService {
  abstract getUncertainty(id: string): Observable<Uncertainty>;
}

@Injectable()
export class UncertaintyMockService extends UncertaintyService {
  private readonly UNCERTAINTY: Uncertainty = {
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
          roundComplete: false,
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

  getUncertainty(id: string): Observable<Uncertainty> {
    return new BehaviorSubject(this.UNCERTAINTY).asObservable();
  }
}
