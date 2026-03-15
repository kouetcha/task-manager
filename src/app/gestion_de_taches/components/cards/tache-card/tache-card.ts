import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EditableTacheDto, EmailDto, FichierInfo} from '../../../interfaces/base-entity-gestion';
import { FileList } from '../../files/file-list/file-list';
import { MaterialModule } from '../../../material.module';
import { RouterLink } from '@angular/router';
import { SafeResourceUrl } from '@angular/platform-browser';
import { PdfModal } from '../pdf-modal/pdf-modal';
import { FichierService } from '../../../services/fichier-service';
import { EmailList } from '../../lists/email-list/email-list';
import { EditableDate } from '../editable-date/editable-date';
import { EditableText } from '../editable-text/editable-text';
import { NotificationService } from '../../../services/notification.service';
import { OnlyOfficeViewer } from '../onlyoffice-viewer/onlyoffice-viewer';
import { Tache } from '../../../models/tache.model';
import { User } from '../../../models/user';

@Component({
  selector: 'app-tache-card',
  imports: [FileList,MaterialModule,RouterLink,PdfModal,EmailList,EditableDate,EditableText,OnlyOfficeViewer],
  templateUrl: './tache-card.html',
  styleUrl: './tache-card.css',
})
export class TacheCard {
  @Input() tache!: Tache;
  @Output() edit = new EventEmitter<Tache>();
  @Output() delete = new EventEmitter<Tache>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
  @Output() editableChamps=new EventEmitter<EditableTacheDto>
  @Output() editEmail = new EventEmitter<EmailDto>(); // pour remonter l'événement d'édition d'email
  @Input() user!: User;
     // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  fichierInfo:FichierInfo|null=null;
  currentDocumentId?: number;
  constructor(
     private notification: NotificationService
  ){

  }

 isCreateur():boolean{
   return this.user.id===this.tache.createur?.id
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      EN_COURS: 'En cours',
      TERMINE: 'Terminé',
      ANNULE: 'Annulé'
    };
    return labels[status] || status;
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      EN_COURS: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      TERMINE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      ANNULE: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    };
    return colors[status] || 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
  }

  onEdit(): void {
    this.edit.emit(this.tache);
  }
  onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }

  onDelete(): void {
    this.delete.emit(this.tache);
  }

  onEditEmail(email: EmailDto): void {
    this.editEmail.emit(email);
  }
  

onEmailAdded(email: EmailDto, tache: Tache) {
  if (!tache.emails) tache.emails = [];
  tache.emails.push(email);
}

onEmailUpdated(email: EmailDto, tache: Tache) {
  const index = tache.emails?.findIndex(e => e.id === email.id) ?? -1;
  if (index !== -1) tache.emails![index] = email;
}

onEmailDeleted(id: number, tache: Tache) {
  tache.emails = tache.emails?.filter(e => e.id !== id) ?? [];
}
  onViewerError(error: string): void {
  console.error('Erreur du viewer ONLYOFFICE:', error);
  // Afficher une notification à l'utilisateur
}

  // Fermeture des modals
  onPdfModalClosed(): void {
    this.isPdfModalOpen = false;
    this.pdfPreviewUrl = '';
  }

   onViewFile(file: FichierInfo, parentType: string, parentId: number): void {
      // Construire l'URL de visualisation (à adapter selon votre API)
      const fileUrl =file.url; 
      const extension = file.nomFichier.split('.').pop()?.toLowerCase();
  
      this.fileName = file.nomFichier;
  
      if (extension === 'pdf') {
        this.pdfPreviewUrl = fileUrl;
        this.isPdfModalOpen = true;
      } else {
    this.onlyOfficeFileUrl = fileUrl;
    this.fileName = file.nomFichier;
    this.currentDocumentId = file.id;
    this.isOnlyOfficeModalOpen = true;
    this.fichierInfo=file;
  }
  
    }
 

  

  onOnlyOfficeModalClosed(): void {
    this.isOnlyOfficeModalOpen = false;
    this.onlyOfficeFileUrl = null;
    this.fichierInfo=null;
  }
    onDeleteFile(file: FichierInfo): void {
      if (confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) {
       
    }
  }
    // Méthodes d'édition inline
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