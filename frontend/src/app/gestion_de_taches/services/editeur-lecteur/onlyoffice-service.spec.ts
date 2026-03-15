import { TestBed } from '@angular/core/testing';

import { OnlyofficeService } from './onlyoffice-service';

describe('OnlyofficeService', () => {
  let service: OnlyofficeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OnlyofficeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
