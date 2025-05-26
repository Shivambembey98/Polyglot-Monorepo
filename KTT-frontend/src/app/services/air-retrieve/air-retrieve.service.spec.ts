import { TestBed } from '@angular/core/testing';

import { AirRetrieveService } from './air-retrieve.service';

describe('AirRetrieveService', () => {
  let service: AirRetrieveService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AirRetrieveService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
