import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { EmailList } from '../lists/email-list/email-list';
import { EditableDate } from '../cards/editable-date/editable-date';
import { EditableText } from '../cards/editable-text/editable-text';
import { Activite } from '../../models/activite.model';
import { EditableActivieDto, EditableDto, EmailDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { SafeResourceUrl } from '@angular/platform-browser';
import { ActiviteService } from '../../services/activite.service';
import { NotificationService } from '../../services/notification.service';
import { FileList } from '../files/file-list/file-list';
import { CommentaireList } from '../commentaires/commentaire-list/commentaire-list';


@Component({
  selector: 'app-activite-detail',
  imports: [EmailList,MaterialModule,CommonModule,FileList,EditableDate,EditableText,CommentaireList],
  templateUrl: './activite-detail.html',
  styleUrl: './activite-detail.css',
})
export class ActiviteDetail implements OnInit{
  @Input() activite!:Activite;
  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<FichierInfo>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
  @Output() activiteModofier=new EventEmitter<Activite>();
  @Input() currentUserId!:number;
  
  @Input() currentUserProfilePicture:string='';

  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  @Output() editableChamps=new EventEmitter<EditableActivieDto>
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  currentDocumentId?: number;
  isCollapsed= false;
   currentPage: number = 1;
   @Input() pageSize: number = 3; 
   @Input() files: FichierInfo[] = []; 


    constructor(
  
    private activiteService: ActiviteService,
    private notification:NotificationService

  ) {
  
  }

    isCreateur():boolean{
   return this.currentUserId===this.activite.createur?.id
  }
  ngOnInit(): void {

    if (!this.activite) {
      this.notification.error('Aucune activité à afficher.');
    }
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

  onEmailAdded(email: EmailDto, activite: Activite) {
    if (!activite.emails) activite.emails = [];
    activite.emails.push(email);
  }
  
  onEmailUpdated(email: EmailDto, activite: Activite) {
    const index = activite.emails?.findIndex(e => e.id === email.id) ?? -1;
    if (index !== -1) activite.emails![index] = email;
  }
    get paginatedFiles(): FichierInfo[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.files.slice(start, start + this.pageSize);
  }

  onEmailDeleted(id: number, activite: Activite) {
    activite.emails = activite.emails?.filter(e => e.id !== id) ?? [];
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
   
     this.activite.designation = newValue.trim();
     const editableDto: EditableActivieDto={
        type:'DESIGNATION',
        activite:this.activite,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
     onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }
   onDescriptionChange(newValue: string): void {
    
     this.activite.description = newValue.trim(); 
       const editableDto: EditableActivieDto={
        type:'DESCRIPTION',
        activite:this.activite,
        texte:newValue.trim()
     }
     this.editableChamps.emit(editableDto);
   }
   
    onDateDebutChange(newValue: Date): void {
       if (!newValue) return;
   
       const debut = new Date(newValue);
       debut.setHours(0, 0, 0, 0);
   
       if (this.activite.dateFin) {
         const fin = new Date(this.activite.dateFin);
         fin.setHours(0, 0, 0, 0);
         if (debut >= fin) {
           this.notification.error('La date de début doit être antérieure à la date de fin.');
           return;
         }
       }
   
       this.activite.dateDebut = newValue;
       const editableDto: EditableActivieDto={
        type:'DATE_DEBUT',
        activite:this.activite,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }

     onDateFinChange(newValue: Date): void {
       if (!newValue) return;
   
       const fin = new Date(newValue);
       fin.setHours(0, 0, 0, 0);
   
       if (this.activite.dateDebut) {
         const debut = new Date(this.activite.dateDebut);
         debut.setHours(0, 0, 0, 0);
         if (fin <= debut) {
           this.notification.error('La date de fin doit être postérieure à la date de début.');
           return;
         }
       }
   
       this.activite.dateFin = newValue;
   
       const editableDto: EditableActivieDto={
        type:'DATE_FIN',
        activite:this.activite,
        date:newValue
     }
     this.editableChamps.emit(editableDto);
     }
}
