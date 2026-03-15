import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdfModal } from './pdf-modal';

describe('PdfModal', () => {
  let component: PdfModal;
  let fixture: ComponentFixture<PdfModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PdfModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdfModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
