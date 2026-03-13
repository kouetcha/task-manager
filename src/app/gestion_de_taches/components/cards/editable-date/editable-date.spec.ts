import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditableDate } from './editable-date';

describe('EditableDate', () => {
  let component: EditableDate;
  let fixture: ComponentFixture<EditableDate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditableDate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditableDate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
