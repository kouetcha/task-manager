import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EmailList } from '../../lists/email-list/email-list';
import { EditableDto, EmailDto, FichierInfo, Projet } from '../../../interfaces/base-entity-gestion';
import { MaterialModule } from '../../../material.module';
import { CommonModule } from '@angular/common';
import { FileList } from '../../files/file-list/file-list';
import { Observable } from 'rxjs';
import { ProjetService } from '../../../services/projet-service';
import { SafeResourceUrl } from '@angular/platform-browser';
import { NotificationService } from '../../../services/notification.service';
import { EditableDate } from '../../cards/editable-date/editable-date';
import { EditableText } from '../../cards/editable-text/editable-text';
import { CommentaireList } from '../../commentaires/commentaire-list/commentaire-list';

@Component({
  selector: 'app-projet-card-detail',
  imports: [EmailList,MaterialModule,CommonModule,FileList,EditableDate,EditableText,CommentaireList],
  templateUrl: './projet-card-detail.html',
  styleUrl: './projet-card-detail.css',
})
export class ProjetCardDetail {
  @Input() projet!:Projet;
  @Input() currentUserId:number=0;
  @Input() currentUserProfilePicture:string='';
  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();

  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  @Output() editableChamps=new EventEmitter<EditableDto>
   @Output() ajoutFichiers = new EventEmitter<File[]>();
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  currentDocumentId?: number;
  isCollapsed= false;
   currentPage: number = 1;
   @Input() pageSize: number = 3; 
   @Input() files: FichierInfo[] = []; 


    constructor(
  
    private projetService: ProjetService,
    private notification:NotificationService

  ) {
  
  }
  isCreateur():boolean{
   return this.currentUserId===this.projet.createur?.id
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

  onEmailAdded(email: EmailDto, projet: Projet) {
    if (!projet.emails) projet.emails = [];
    projet.emails.push(email);
  }
  
  onEmailUpdated(email: EmailDto, projet: Projet) {
    const index = projet.emails?.findIndex(e => e.id === email.id) ?? -1;
    if (index !== -1) projet.emails![index] = email;
  }
    get paginatedFiles(): FichierInfo[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.files.slice(start, start + this.pageSize);
  }

  onEmailDeleted(id: number, projet: Projet) {
    projet.emails = projet.emails?.filter(e => e.id !== id) ?? [];
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
   
     this.projet.designation = newValue.trim();
     const editableDto: EditableDto={
        type:'DESIGNATION',
        projet:this.projet,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
   
   onDescriptionChange(newValue: string): void {
    
     this.projet.description = newValue.trim(); 
       const editableDto: EditableDto={
        type:'DESCRIPTION',
        projet:this.projet,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
   
    onDateDebutChange(newValue: Date): void {
       if (!newValue) return;
   
       const debut = new Date(newValue);
       debut.setHours(0, 0, 0, 0);
   
       if (this.projet.dateFin) {
         const fin = new Date(this.projet.dateFin);
         fin.setHours(0, 0, 0, 0);
         if (debut >= fin) {
           this.notification.error('La date de début doit être antérieure à la date de fin.');
           return;
         }
       }
   
       this.projet.dateDebut = newValue;
       const editableDto: EditableDto={
        type:'DATE_DEBUT',
        projet:this.projet,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }
   
     onDateFinChange(newValue: Date): void {
       if (!newValue) return;
   
       const fin = new Date(newValue);
       fin.setHours(0, 0, 0, 0);
   
       if (this.projet.dateDebut) {
         const debut = new Date(this.projet.dateDebut);
         debut.setHours(0, 0, 0, 0);
         if (fin <= debut) {
           this.notification.error('La date de fin doit être postérieure à la date de début.');
           return;
         }
       }
   
       this.projet.dateFin = newValue;
   
       const editableDto: EditableDto={
        type:'DATE_FIN',
        projet:this.projet,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }

    onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }



}
