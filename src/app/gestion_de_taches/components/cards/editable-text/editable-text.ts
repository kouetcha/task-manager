import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
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
export class EditableText implements OnChanges {
  @Input() value: string = '';
  @Input() multiline: boolean = false;
  @Input() isCreateur!: boolean;
  @Input() maxLength: number = 0; // 0 = pas de troncature

  @Output() valueChange = new EventEmitter<string>();

  editing = false;
  editedValue: string = '';
  expanded = false;

  get isTruncatable(): boolean {
    return this.maxLength > 0 && this.value?.length > this.maxLength;
  }

  get displayValue(): string {
    if (!this.isTruncatable || this.expanded) return this.value;
    return this.value.slice(0, this.maxLength) + '…';
  }

  ngOnChanges(): void {
    this.expanded = false;
  }

  toggleExpanded(event: MouseEvent): void {
    event.stopPropagation();
    this.expanded = !this.expanded;
  }

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