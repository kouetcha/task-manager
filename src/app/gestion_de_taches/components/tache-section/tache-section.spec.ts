import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TacheSection } from './tache-section';

describe('TacheSection', () => {
  let component: TacheSection;
  let fixture: ComponentFixture<TacheSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TacheSection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TacheSection);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
