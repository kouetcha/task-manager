import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, QueryList, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CommentairesService } from '../../../services/commentaires.service';
import { Commentaire, CreateCommentaireDto, ELEMENTTYPE, FichierInfo } from '../../../interfaces/base-entity-gestion';
import { finalize, switchMap } from 'rxjs/operators';
import { CommentaireCard } from '../commentaire-card/commentaire-card';
import { FichierCommentaireService } from '../../../services/fichier-commentaire.service';
import { NotificationService } from '../../../services/notification.service';


@Component({
  selector: 'app-commentaire-list',
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    CommentaireCard,
  ],
  templateUrl: './commentaire-list.html',
  styleUrl: './commentaire-list.css',
})
export class CommentaireList implements OnChanges, OnInit{
 @Input() parentId!: number;
  @Input() typeElem!: ELEMENTTYPE;
  @Input() isCreateur!: boolean;
  @Input() currentUserId!: number; 
  @Input() currentUserProfilePicture?: string;
  @Output() viewFile = new EventEmitter<FichierInfo>();

  @ViewChild('scrollContainer') scrollContainer!: ElementRef;
  @ViewChild('lastComment') lastComment!: ElementRef;

  commentaires: Commentaire[] = [];
  loading = false;
  newCommentContent = '';
  selectedFiles: File[] = [];
  collapsed = false;

  constructor(private commentairesService: CommentairesService, 
    private fichierService: FichierCommentaireService,
    private notification:NotificationService
    , private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    console.log("PARENT ID")
    console.log(this.parentId)
     console.log(" ELEMENTTYPE")
    console.log(this.typeElem)
    this.loadComments();
  }
ngOnChanges(changes: SimpleChanges): void {
  if (changes['parentId'] || changes['typeElem']) {
    this.loadComments();
  }
}
    onView(file: FichierInfo): void {
      this.viewFile.emit(file);
    }

loadComments(): void {
  const cached = this.commentairesService.getCached(this.typeElem, this.parentId);
  if (cached) {
    console.log("cached")
     console.log(cached)
    this.commentaires = cached;
    this.loading = false;
    this.scrollToLast();
    return; 
  }

  this.commentaires = []; 
  this.loading = true;

  this.commentairesService
    .getByParent(this.typeElem, this.parentId)
    .pipe(finalize(() => (this.loading = false)))
    .subscribe({
      next: (comments) => {
        this.commentaires = comments;
        this.loading = false
        this.cdr.detectChanges();
        this.scrollToLast();
      },
      error: (err) => console.error('Erreur chargement commentaires', err),
    });
}
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }
scrollToLast(): void {
  setTimeout(() => {
    const container = this.scrollContainer?.nativeElement;
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }, 200);
}



  addComment(): void {
    if (!this.newCommentContent.trim() && this.selectedFiles&&this.selectedFiles.length === 0) return;
      console.log('this.selectedFiles')
     console.log(this.selectedFiles)
    const dto: CreateCommentaireDto = {
      contenu: this.newCommentContent.trim(),
      auteurId: this.currentUserId,
      parentId: this.parentId,
    };

    this.commentairesService
      .create(this.typeElem, dto, this.selectedFiles)
      .subscribe({
        next: (newComment) => {
          this.commentaires = [ ...this.commentaires,newComment]; // Ajout en tête
          this.newCommentContent = '';
          this.selectedFiles = [];
          this.cdr.detectChanges();
            this.scrollToLast(); 
            this.notification.success("commentaire ajouté avec succès")
          
        },
        error: (err) => console.error('Erreur ajout commentaire', err),
      });
  }
 onFocusInput(): void {
    this.scrollToLast();
  }
  onCommentUpdated(updatedComment: Commentaire): void {
    this.commentairesService
      .changeContenu(this.typeElem, { id: updatedComment.id, contenu: updatedComment.contenu })
      .subscribe({
        next: (comment) => {
          const index = this.commentaires.findIndex((c) => c.id === comment.id);
          if (index !== -1) this.commentaires[index] = comment;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Erreur modification', err),
      });
  }

  onCommentDeleted(commentId: number): void {
    this.commentairesService.delete(this.typeElem, commentId,this.parentId).subscribe({
      next: () => {
        this.commentaires = this.commentaires.filter((c) => c.id !== commentId);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erreur suppression', err),
    });
  }

  private updateCommentFiles(commentId: number, fichiers: FichierInfo[]) {
  const index = this.commentaires.findIndex(c => c.id === commentId);

  if (index !== -1) {
    this.commentaires[index] = {
      ...this.commentaires[index],
      fichiers
    };

    this.commentaires = [...this.commentaires]; // trigger change detection
  }
} 

autoResize(event: Event): void {
  const textarea = event.target as HTMLTextAreaElement;
  textarea.style.height = 'auto';
  textarea.style.height = textarea.scrollHeight + 'px';
}
  toggleCollapse(): void {
    this.collapsed = !this.collapsed;
    if (!this.collapsed) {
      this.scrollToLast(); // scroll quand on déplie
    }
  }

     onAddFiles(commentaire:Commentaire, files: File[]): void {
       this.fichierService.uploadFiles(this.typeElem, commentaire.id, files).pipe(
         switchMap(() => this.fichierService.listFiles(this.typeElem, commentaire.id))
       ).subscribe({
         next: (fichiers) => {
           const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
             id: f.id,
             nomFichier: f.nomFichier ?? '',
             url: f.url ?? '',
             type: f.type ?? '',
             callbackurl:f.callbackurl??''
           }));
           commentaire=Object.assign( commentaire, { fichiers: updatedFichiers });
     
          this.updateCommentFiles(commentaire.id, updatedFichiers);
           this.notification.success("Fichier(s) ajouté(s) avec succès");
         
           this.cdr.detectChanges();
         },
         error: (err) => {
           console.error('Erreur upload/récupération fichiers:', err);
           this.notification.error("Erreur lors de l'upload");
         }
       });
     }
onDelete(file: FichierInfo, parentId: number): void {

  if (!confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) return;

  this.fichierService.deleteFile(this.typeElem, parentId, file.id).pipe(
    switchMap(() => this.fichierService.listFiles(this.typeElem, parentId))
  ).subscribe({
    next: (fichiers) => {

      const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
        id: f.id,
        nomFichier: f.nomFichier ?? '',
        url: f.url ?? '',
        type: f.type ?? '',
           callbackurl:f.callbackurl??''
      }));

      this.updateCommentFiles(parentId, updatedFichiers);

      this.notification.success("Fichier supprimé avec succès");
    },
    error: (err) => {
      console.error('Erreur suppression fichier', err);
      this.notification.error("Erreur lors de la suppression");
    }
  });
}

}
