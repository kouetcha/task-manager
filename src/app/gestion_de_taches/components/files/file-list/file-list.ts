import { Component, EventEmitter, Input, Output, ViewChild, ElementRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FichierInfo } from '../../../interfaces/base-entity-gestion';
import { FileListDialog } from '../../dialogs/file-list-dialog/file-list-dialog';
import { MaterialModule } from '../../../material.module';
import { FileCard } from '../../cards/file-card/file-card';


@Component({
  selector: 'app-file-list',
  templateUrl: './file-list.html',
  styleUrls: ['./file-list.css'],
  imports:[MaterialModule,FileCard]
})
export class FileList {
  @Input() files: FichierInfo[] = [];
  @Input() pageSize: number = 3;               // nombre d'éléments par page
  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();
  @Output() addFiles = new EventEmitter<File[]>();  // émet les fichiers sélectionnés

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  currentPage: number = 1;

  constructor(private dialog: MatDialog) {}

  get totalPages(): number {
    return Math.ceil(this.files.length / this.pageSize);
  }

  get paginatedFiles(): FichierInfo[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.files.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  onView(file: FichierInfo): void {
    this.viewFile.emit(file);
  }

  onDelete(file: FichierInfo): void {
    this.deleteFile.emit(file);
    // Après suppression, si la page courante devient vide, reculer d'une page
    if (this.paginatedFiles.length === 0 && this.currentPage > 1) {
      this.currentPage--;
    }
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      this.addFiles.emit(files);
      // Réinitialiser l'input pour permettre de resélectionner les mêmes fichiers
      this.fileInput.nativeElement.value = '';
    }
  }
  

  openViewAllModal(): void {
    const dialogRef = this.dialog.open(FileListDialog, {
      width: '600px',
      data: { files: this.files }
    });

    // Souscrire aux événements de la modale pour les répercuter
    dialogRef.componentInstance.viewFile.subscribe((file: FichierInfo) => {
      this.viewFile.emit(file);
    });
    dialogRef.componentInstance.deleteFile.subscribe((file: FichierInfo) => {
      this.deleteFile.emit(file);
    });
    dialogRef.componentInstance.addFiles.subscribe((files: File[]) => {
      this.addFiles.emit(files);
    });
  }
}