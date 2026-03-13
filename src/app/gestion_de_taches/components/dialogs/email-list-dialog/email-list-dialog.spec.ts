import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailListDialog } from './email-list-dialog';

describe('EmailListDialog', () => {
  let component: EmailListDialog;
  let fixture: ComponentFixture<EmailListDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailListDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmailListDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
