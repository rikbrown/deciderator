import { Injectable } from '@angular/core';
import {DecideratorSocketService} from '../deciderator-socket/deciderator-socket.service';

@Injectable({
  providedIn: 'root'
})
export class UsernameService {
  private static readonly storage = window.localStorage;

  constructor(private decideratorSocketService: DecideratorSocketService) {
    this.setUsernameFromStorage();
  }

  getUsernameFromStorage(): string | null {
    return UsernameService.storage.getItem('username');
  };

  setUsername(username: string): void {
    const request: SetUsernameRequest = { username };
    this.decideratorSocketService.send('SetUsernameRequest', request);
    UsernameService.storage.setItem('username', username);
  }

  private setUsernameFromStorage(): void {
    const storageUsername: string = this.getUsernameFromStorage();
    if (storageUsername != null) {
      console.info(`Found username ${storageUsername} in local storage, setting in session`);
      this.setUsername(storageUsername);
    }
  }
}
