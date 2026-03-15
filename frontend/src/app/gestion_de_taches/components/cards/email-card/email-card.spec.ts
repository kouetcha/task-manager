import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailCard } from './email-card';

describe('EmailCard', () => {
  let component: EmailCard;
  let fixture: ComponentFixture<EmailCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmailCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
