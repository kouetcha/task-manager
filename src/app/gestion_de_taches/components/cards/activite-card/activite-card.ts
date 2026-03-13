import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EditableActivieDto, EmailDto, FichierInfo} from '../../../interfaces/base-entity-gestion';
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
import { Activite } from '../../../models/activite.model';

@Component({
  selector: 'app-activite-card',
  imports: [FileList,MaterialModule,RouterLink,PdfModal,EmailList,EditableDate,EditableText,OnlyOfficeViewer],
  templateUrl: './activite-card.html',
  styleUrl: './activite-card.css',
})
export class ActiviteCard {
  @Input() activite!: Activite;
  @Output() edit = new EventEmitter<Activite>();
  @Output() delete = new EventEmitter<Activite>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
  @Output() editableChamps=new EventEmitter<EditableActivieDto>
  @Output() editEmail = new EventEmitter<EmailDto>(); // pour remonter l'événement d'édition d'email
     // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  fichierInfo:FichierInfo|null=null;
  currentDocumentId?: number;
  constructor(private fichierService:FichierService,
     private notification: NotificationService
  ){

  }
  // Fonctions pour les statuts (à adapter selon votre logique métier)
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
    this.edit.emit(this.activite);
  }
  onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }

  onDelete(): void {
    this.delete.emit(this.activite);
  }

  onEditEmail(email: EmailDto): void {
    this.editEmail.emit(email);
  }
  

onEmailAdded(email: EmailDto, activite: Activite) {
  if (!activite.emails) activite.emails = [];
  activite.emails.push(email);
}

onEmailUpdated(email: EmailDto, activite: Activite) {
  const index = activite.emails?.findIndex(e => e.id === email.id) ?? -1;
  if (index !== -1) activite.emails![index] = email;
}

onEmailDeleted(id: number, activite: Activite) {
  activite.emails = activite.emails?.filter(e => e.id !== id) ?? [];
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

  this.activite.designation = newValue.trim();
  const editableDto: EditableActivieDto={
     type:'DESIGNATION',
     activite:this.activite,
     texte:newValue.trim()
  }
  this.editableChamps.emit(editableDto);
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