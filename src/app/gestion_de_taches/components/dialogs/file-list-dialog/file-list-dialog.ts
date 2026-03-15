import { Component, Inject, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FichierInfo } from '../../../interfaces/base-entity-gestion';
import { MaterialModule } from '../../../material.module';
import { FileCard } from '../../cards/file-card/file-card';

@Component({
  selector: 'app-file-list-dialog',
  templateUrl: './file-list-dialog.html',
  styleUrls: ['./file-list-dialog.css'],
  imports:[MaterialModule,FileCard]
})
export class FileListDialog {
  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();
  @Output() addFiles = new EventEmitter<File[]>();

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { files: FichierInfo[];isCreateur:boolean }) {}

  onView(file: FichierInfo): void {
    this.viewFile.emit(file);
  }

  onDelete(file: FichierInfo): void {
    this.deleteFile.emit(file);
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      this.addFiles.emit(files);
      this.fileInput.nativeElement.value = '';
    }
  }
}