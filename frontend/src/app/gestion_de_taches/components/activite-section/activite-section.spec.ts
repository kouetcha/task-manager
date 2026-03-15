import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiviteSection } from './activite-section';

describe('ActiviteSection', () => {
  let component: ActiviteSection;
  let fixture: ComponentFixture<ActiviteSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiviteSection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiviteSection);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
