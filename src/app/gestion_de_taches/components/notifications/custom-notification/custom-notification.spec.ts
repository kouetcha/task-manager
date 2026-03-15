import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomNotification } from './custom-notification';

describe('CustomNotification', () => {
  let component: CustomNotification;
  let fixture: ComponentFixture<CustomNotification>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomNotification]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomNotification);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
