import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiviteForm } from './activite-form';

describe('ActiviteForm', () => {
  let component: ActiviteForm;
  let fixture: ComponentFixture<ActiviteForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiviteForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiviteForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
