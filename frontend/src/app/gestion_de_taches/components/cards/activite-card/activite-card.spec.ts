import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiviteCard } from './activite-card';

describe('ActiviteCard', () => {
  let component: ActiviteCard;
  let fixture: ComponentFixture<ActiviteCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiviteCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiviteCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
