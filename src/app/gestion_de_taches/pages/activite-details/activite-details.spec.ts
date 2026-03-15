import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiviteDetails } from './activite-details';

describe('ActiviteDetails', () => {
  let component: ActiviteDetails;
  let fixture: ComponentFixture<ActiviteDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiviteDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiviteDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
