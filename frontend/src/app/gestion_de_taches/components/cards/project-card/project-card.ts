import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { EditableDto, EmailDto, FichierInfo, Projet } from '../../../interfaces/base-entity-gestion';
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
import { User } from '../../../models/user';
import { ProjetService } from '../../../services/projet-service';
import { WebSocketService } from '../../../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-project-card',
  imports: [FileList,MaterialModule,RouterLink,PdfModal,EmailList,EditableDate,EditableText,OnlyOfficeViewer],
  templateUrl: './project-card.html',
  styleUrl: './project-card.css',
})
export class ProjectCard {
  @Input() projet!: Projet;
  @Input() user!: User;
  @Output() edit = new EventEmitter<Projet>();
  @Output() delete = new EventEmitter<Projet>();
  @Output() updateProjet = new EventEmitter<Projet>();
  @Output() ajoutFichiers = new EventEmitter<File[]>();
  @Output() editableChamps=new EventEmitter<EditableDto>
  @Output() editEmail = new EventEmitter<EmailDto>(); // pour remonter l'événement d'édition d'email
     // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  fichierInfo:FichierInfo|null=null;
  currentDocumentId?: number;
  
  // ── Services ─────────────────────────────────────────────────
  private projetService  = inject(ProjetService);
  private notification   = inject(NotificationService);
  private wsService      = inject(WebSocketService);
  private subs           = new Subscription();


    // ── Lifecycle ────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadProjet();

    // Écoute les mises à jour WebSocket de ce projet
    const sub = this.wsService
      .onProjetUpdate(this.projet.id)
      .subscribe(event => {
        console.log('📦 Mise à jour projet reçue :', event);
        if (event.type === 'PROJET_MODIFIE') {
          this.loadProjet();
          this.updateProjet.emit(this.projet);
        }
      });

    this.subs.add(sub);
  }
    // ── Chargement ───────────────────────────────────────────────
  loadProjet(): void {
    this.projetService.getProjetById(this.projet.id).subscribe({
      next:  data  => this.projet = data,
      error: err   => console.error('Erreur chargement projet', err)
    });
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

  isCreateur():boolean{
   return this.user.id===this.projet.createur?.id
  }

  onEdit(): void {
    this.edit.emit(this.projet);
  }
  onAddFichiers(fichiers:File[]):void{
    this.ajoutFichiers.emit(fichiers);
  }

  onDelete(): void {
    this.delete.emit(this.projet);
  }

  onEditEmail(email: EmailDto): void {
    this.editEmail.emit(email);
  }
  

onEmailAdded(email: EmailDto, projet: Projet) {
  if (!projet.emails) projet.emails = [];
  projet.emails.push(email);
}

onEmailUpdated(email: EmailDto, projet: Projet) {
  const index = projet.emails?.findIndex(e => e.id === email.id) ?? -1;
  if (index !== -1) projet.emails![index] = email;
}

onEmailDeleted(id: number, projet: Projet) {
  projet.emails = projet.emails?.filter(e => e.id !== id) ?? [];
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




    

    
  
  
  
}