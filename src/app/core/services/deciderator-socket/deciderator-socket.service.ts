import {Injectable, OnInit} from '@angular/core';
import * as SockJS from 'sockjs-client';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';

const SERVER_URL = 'http://localhost:8080/handler';

@Injectable({
  providedIn: 'root'
})

@Injectable()
export class DecideratorSocketService {
  activeSessionsMessageSubject: Subject<ActiveSessionsMessage> = new BehaviorSubject(null);
  uncertaintyCreatedMessageSubject: Subject<UncertaintyCreatedMessage> = new Subject();
  uncertaintyDetailsMessageSubject: Subject<UncertaintyDetailsMessage> = new Subject();
  uncertaintyUsersMessageSubject: Subject<UncertaintyUsersMessage> = new Subject();
  uncertaintyJoinedMessageSubject: Subject<UncertaintyJoinedMessage> = new Subject();
  private openPromise: ReplaySubject<void> = new ReplaySubject(1);
  private socket = new SockJS(SERVER_URL);

  constructor() {
    this.socket.onopen = () => {
      this.openPromise.next(null);
      console.log('oh lawd we open');
    };
    this.socket.onmessage = (evt) => {
      const data = JSON.parse(evt.data);
      const clazz = data['@class'].replace('codes.rik.deciderator.types.Messages$', '');
      console.log(clazz, data);
      this.getSubject(clazz)?.next(data);
    };
  }

  public send(type: string, request: UncertaintyRequest) {
    request['@class'] = 'codes.rik.deciderator.types.Messages$' + type;
    this.openPromise.subscribe(() => this.socket.send(JSON.stringify(request)));
  }

  private getSubject(clazz: string): Subject<UncertaintyMessage> {
    switch (clazz) {
      case 'ActiveSessionsMessage': {
        return this.activeSessionsMessageSubject;
      }
      case 'UncertaintyCreatedMessage': {
        return this.uncertaintyCreatedMessageSubject;
      }
      case 'UncertaintyDetailsMessage': {
        return this.uncertaintyDetailsMessageSubject;
      }
      case 'UncertaintyUsersMessage': {
        return this.uncertaintyUsersMessageSubject;
      }
      case 'UncertaintyJoinedMessage': {
        return this.uncertaintyJoinedMessageSubject;
      }
      default: {
        console.warn(`Ignoring unknown message: ${clazz}`);
        return null;
      }
    }
  }

  // public send(message: Message): void {
  //   this.socket.emit('message', message);
  // }

  // public onMessage(): Observable<Message> {
  //   return new Observable<Message>(observer => {
  //     this.socket.on('message', (data: Message) => observer.next(data));
  //   });
  // }
  //

}
