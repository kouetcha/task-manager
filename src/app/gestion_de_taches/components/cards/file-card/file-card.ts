import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FichierInfo } from '../../../interfaces/base-entity-gestion';
import { MaterialModule } from '../../../material.module';


@Component({
  selector: 'app-file-card',
  templateUrl: './file-card.html',
  styleUrls: ['./file-card.css'],
  imports:[MaterialModule]
})
export class FileCard {
  @Input() file!: FichierInfo;
  @Input() isCreateur!: boolean;
  @Output() view = new EventEmitter<FichierInfo>();
  @Output() delete = new EventEmitter<FichierInfo>();

  getFileIcon(): string {
    const extension = this.file.nomFichier.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'pdf': return 'picture_as_pdf';
      case 'doc':
      case 'docx': return 'description';
      case 'xls':
      case 'xlsx': return 'table_chart';
      case 'ppt':
      case 'pptx': return 'slideshow';
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif': return 'image';
      default: return 'attach_file';
    }
  }

  onView(): void {
    this.view.emit(this.file);
  }

  onDelete(): void {
    this.delete.emit(this.file);
  }
}