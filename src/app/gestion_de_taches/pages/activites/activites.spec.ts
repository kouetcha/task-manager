import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Activites } from './activites';

describe('Activites', () => {
  let component: Activites;
  let fixture: ComponentFixture<Activites>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Activites]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Activites);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
