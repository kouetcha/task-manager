import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiviteDetail } from './activite-detail';

describe('ActiviteDetail', () => {
  let component: ActiviteDetail;
  let fixture: ComponentFixture<ActiviteDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiviteDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiviteDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
