import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TacheDetails } from './tache-details';

describe('TacheDetails', () => {
  let component: TacheDetails;
  let fixture: ComponentFixture<TacheDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TacheDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TacheDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
