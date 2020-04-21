import { TestBed } from '@angular/core/testing';

import { DecideratorSocketService } from './deciderator-socket.service';

describe('DecideratorClientService', () => {
  let service: DecideratorSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DecideratorSocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
