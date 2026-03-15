import { ChangeDetectionStrategy } from '@angular/core';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { Commentaire, ELEMENTTYPE, FichierInfo } from '../../../interfaces/base-entity-gestion';
import { FileList } from '../../files/file-list/file-list';
@Component({
  selector: 'app-commentaire-card',
  templateUrl: './commentaire-card.html',
   imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatMenuModule,
    MatButtonModule,
    FileList
  ],
  styleUrls: ['./commentaire-card.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommentaireCard {
   @Input() commentaire!: Commentaire;
  @Input() currentUserId?: number; 
  @Input() typeElem!: ELEMENTTYPE; 
  @Output() deleted = new EventEmitter<number>();
  @Output() updated = new EventEmitter<Commentaire>();
   @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
   currentPage: number = 1;
   @Input() pageSize: number = 3; 
      @Input() files: FichierInfo[] = []; 
     get paginatedFiles(): FichierInfo[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.files.slice(start, start + this.pageSize);
  }


  editing = false;
  editedContent = '';

  get isOwner(): boolean {
    if(this.commentaire.auteur==undefined) return true
   
    return this.currentUserId === this.commentaire.auteur.id;
  }

  startEdit(): void {
    this.editedContent = this.commentaire.contenu;
    this.editing = true;
  }

  saveEdit(): void {
    if (this.editedContent.trim() === this.commentaire.contenu) {
      this.editing = false;
      return;
    }
    // Émettre l'événement pour que le parent gère l'appel service
    this.updated.emit({ ...this.commentaire, contenu: this.editedContent });
    this.editing = false;
  }

  cancelEdit(): void {
    this.editing = false;
  }

  deleteComment(): void {
    if (confirm('Supprimer ce commentaire ?')) {
      this.deleted.emit(this.commentaire.id);
    }
  }
   onView(file: FichierInfo): void {
      this.viewFile.emit(file);
    }
    onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }
    onDelete(file: FichierInfo): void {
      this.deleteFile.emit(file);
      // Après suppression, si la page courante devient vide, reculer d'une page
      if (this.paginatedFiles.length === 0 && this.currentPage > 1) {
        this.currentPage--;
      }
    }
}