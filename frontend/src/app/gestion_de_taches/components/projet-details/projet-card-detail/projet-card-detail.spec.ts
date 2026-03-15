import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjetCardDetail } from './projet-card-detail';

describe('ProjetCardDetail', () => {
  let component: ProjetCardDetail;
  let fixture: ComponentFixture<ProjetCardDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjetCardDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjetCardDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
