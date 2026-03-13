import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-editable-text',
   imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatMenuModule],
  templateUrl: './editable-text.html',
  styleUrl: './editable-text.css',
})
export class EditableText {
  @Input() value: string = '';
  @Input() multiline: boolean = false;
  @Output() valueChange = new EventEmitter<string>();

  editing = false;
  editedValue: string = '';

  startEdit(): void {
    this.editedValue = this.value;
    this.editing = true;
  }

  save(): void {
    this.valueChange.emit(this.editedValue);
    this.editing = false;
  }

  cancel(): void {
    this.editing = false;
  }
}
