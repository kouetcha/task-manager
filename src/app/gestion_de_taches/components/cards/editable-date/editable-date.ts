import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-editable-date',
   imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatMenuModule, MatDatepickerModule, MatNativeDateModule],
  templateUrl: './editable-date.html',
  styleUrl: './editable-date.css',
})
export class EditableDate {
  @Input() value: Date | null = null;
  @Input() min?: Date;
  @Input() max?: Date;
  @Output() valueChange = new EventEmitter<Date>();

  editing = false;
  editedValue: Date | null = null;

  startEdit(): void {
    this.editedValue = this.value;
    this.editing = true;
  }

  save(): void {
    if (this.editedValue) {
      this.valueChange.emit(this.editedValue);
    }
    this.editing = false;
  }

  cancel(): void {
    this.editing = false;
  }
}
