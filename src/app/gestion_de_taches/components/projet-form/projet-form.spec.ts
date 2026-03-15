import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjetForm } from './projet-form';

describe('ProjetForm', () => {
  let component: ProjetForm;
  let fixture: ComponentFixture<ProjetForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjetForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjetForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
