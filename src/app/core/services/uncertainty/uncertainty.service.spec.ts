import { TestBed } from '@angular/core/testing';

import { UncertaintyService } from './uncertainty.service';

describe('UncertaintyService', () => {
  let service: UncertaintyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UncertaintyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
