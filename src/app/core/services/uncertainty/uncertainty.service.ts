import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {NewUncertainty} from './types';
import {DecideratorSocketService} from '../deciderator-socket/deciderator-socket.service';
import {filter, first, map, take} from 'rxjs/operators';

@Injectable()
export abstract class UncertaintyService {
  abstract createUncertainty(uncertainty: NewUncertainty): Observable<string>;
  abstract observeUncertainty(id: string, askForDetails?: boolean): Observable<Uncertainty>;
  abstract joinUncertainty(id: string): Observable<Uncertainty>;
  abstract observeUncertaintyUsers(uncertaintyId: string): Observable<UncertaintyUser[]>;
  abstract nextRound(uncertaintyId: string): void;
  abstract leaveUncertainty(id: string): void;
}

@Injectable()
export class DecideratorUncertaintyService implements UncertaintyService {
  constructor(private decideratorSocketService: DecideratorSocketService) {

  }

  createUncertainty(uncertainty: NewUncertainty): Observable<string> {
    const request: CreateUncertaintyRequest = {
      name: uncertainty.name,
      options: uncertainty.options.map(o => o.name)
    };
    this.decideratorSocketService.send('CreateUncertaintyRequest', request);
    return this.decideratorSocketService.uncertaintyCreatedMessageSubject
      .pipe(take(1))
      .pipe(map(msg => msg.uncertaintyId));
  }

  joinUncertainty(id: string): Observable<Uncertainty> {
    const request: JoinUncertaintyRequest = { uncertaintyId: id };
    this.decideratorSocketService.send('JoinUncertaintyRequest', request);

    return this.decideratorSocketService.uncertaintyJoinedMessageSubject
      .pipe(first(msg => msg.uncertainty.id === id))
      .pipe(map(msg => msg.uncertainty));
  }

  observeUncertainty(id: string, askForDetails?: boolean): Observable<Uncertainty> {
    // Send initial request
    if (askForDetails) {
      const request: GetUncertaintyRequest = {uncertaintyId: id};
      this.decideratorSocketService.send('GetUncertaintyRequest', request);
    }

    // Start listening to updates
    return this.decideratorSocketService.uncertaintyDetailsMessageSubject
      .pipe(filter(msg => msg.uncertainty.id === id))
      .pipe(map(msg => msg.uncertainty));
  }

  observeUncertaintyUsers(uncertaintyId: string): Observable<UncertaintyUser[]> {
    return this.decideratorSocketService.uncertaintyUsersMessageSubject
      .pipe(filter(msg => msg.uncertaintyId === uncertaintyId))
      .pipe(map(msg => msg.users.map(user => {
        return {
          username: user,
          self: user === msg.username
        };
      })));
  }



  nextRound(uncertaintyId: string): void {
  }

  leaveUncertainty(id: string): void {
    const request: LeaveUncertaintyRequest = {uncertaintyId: id};
    this.decideratorSocketService.send('LeaveUncertaintyRequest', request);
  }


}

export interface UncertaintyUser {
  username: string;
  self: boolean;
}
