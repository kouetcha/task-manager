import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { EmailList } from '../lists/email-list/email-list';
import { EditableDate } from '../cards/editable-date/editable-date';
import { EditableText } from '../cards/editable-text/editable-text';
import { Tache } from '../../models/tache.model';
import { EditableTacheDto, EditableDto, EmailDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { SafeResourceUrl } from '@angular/platform-browser';
import { TacheService } from '../../services/tache.service';
import { NotificationService } from '../../services/notification.service';
import { FileList } from '../files/file-list/file-list';
import { CommentaireList } from '../commentaires/commentaire-list/commentaire-list';


@Component({
  selector: 'app-tache-detail',
  imports: [EmailList,MaterialModule,CommonModule,FileList,EditableDate,EditableText,CommentaireList],
  templateUrl: './tache-detail.html',
  styleUrl: './tache-detail.css',
})
export class TacheDetail  implements OnInit{
  @Input() tache!:Tache;
  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
  @Output() tacheModofier=new EventEmitter<Tache>();
    @Input() currentUserId:number=0;
    @Input() currentUserProfilePicture:string='';

  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  @Output() editableChamps=new EventEmitter<EditableTacheDto>
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  currentDocumentId?: number;
  isCollapsed= false;
   currentPage: number = 1;
   @Input() pageSize: number = 3; 
   @Input() files: FichierInfo[] = []; 


    constructor(
  
    private tacheService: TacheService,
    private notification:NotificationService

  ) {
  
  }
  ngOnInit(): void {
   console.log("Tache selectionnée ID  "+ this.tache.id);
  }

    getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      EN_ATTENTE: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      EN_COURS: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      TERMINE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      ANNULE: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    };
    return colors[status] || 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
  }

    getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      EN_ATTENTE: 'En attente',
      EN_COURS: 'En cours',
      TERMINE: 'Terminé',
      ANNULE: 'Annulé'
    };
    return labels[status] || status;
  }

  onEmailAdded(email: EmailDto, tache: Tache) {
    if (!tache.emails) tache.emails = [];
    tache.emails.push(email);
  }
  
  onEmailUpdated(email: EmailDto, tache: Tache) {
    const index = tache.emails?.findIndex(e => e.id === email.id) ?? -1;
    if (index !== -1) tache.emails![index] = email;
  }
    get paginatedFiles(): FichierInfo[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.files.slice(start, start + this.pageSize);
  }

  onEmailDeleted(id: number, tache: Tache) {
    tache.emails = tache.emails?.filter(e => e.id !== id) ?? [];
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

   onDesignationChange(newValue: string): void {
     if (!newValue || newValue.trim() === '') {
       this.notification.error("La désignation ne peut pas être vide.");
       return;
     }
   
     this.tache.designation = newValue.trim();
     const editableDto: EditableTacheDto={
        type:'DESIGNATION',
        tache:this.tache,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
     onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }
   onDescriptionChange(newValue: string): void {
    
     this.tache.description = newValue.trim(); 
       const editableDto: EditableTacheDto={
        type:'DESCRIPTION',
        tache:this.tache,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
   
    onDateDebutChange(newValue: Date): void {
       if (!newValue) return;
   
       const debut = new Date(newValue);
       debut.setHours(0, 0, 0, 0);
   
       if (this.tache.dateFin) {
         const fin = new Date(this.tache.dateFin);
         fin.setHours(0, 0, 0, 0);
         if (debut >= fin) {
           this.notification.error('La date de début doit être antérieure à la date de fin.');
           return;
         }
       }
   
       this.tache.dateDebut = newValue;
       const editableDto: EditableTacheDto={
        type:'DATE_DEBUT',
        tache:this.tache,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }

     onDateFinChange(newValue: Date): void {
       if (!newValue) return;
   
       const fin = new Date(newValue);
       fin.setHours(0, 0, 0, 0);
   
       if (this.tache.dateDebut) {
         const debut = new Date(this.tache.dateDebut);
         debut.setHours(0, 0, 0, 0);
         if (fin <= debut) {
           this.notification.error('La date de fin doit être postérieure à la date de début.');
           return;
         }
       }
   
       this.tache.dateFin = newValue;
   
       const editableDto: EditableTacheDto={
        type:'DATE_FIN',
        tache:this.tache,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }
}
