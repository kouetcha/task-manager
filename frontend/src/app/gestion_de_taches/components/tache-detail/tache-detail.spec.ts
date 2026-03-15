import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TacheDetail } from './tache-detail';

describe('TacheDetail', () => {
  let component: TacheDetail;
  let fixture: ComponentFixture<TacheDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TacheDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TacheDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
