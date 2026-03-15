import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import {
  Projet,
  CreateActiviteDto,
  UpdateActiviteDto,
  CreateTacheDto,
  UpdateTacheDto,
  EmailDto,
  FichierInfo
} from '../../interfaces/base-entity-gestion';
import { ProjetService } from '../../services/projet-service';
import { ActiviteService } from '../../services/activite.service';
import { TacheService } from '../../services/tache.service';
import { Activite } from '../../models/activite.model';
import { Tache } from '../../models/tache.model';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/AuthService';
import { environment } from '../../../../../environment';
import { MaterialModule } from '../../material.module';
import { FileList } from '../../components/files/file-list/file-list';
import { EmailCard } from '../../components/cards/email-card/email-card';
import { FormsModule } from '@angular/forms';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { SafeResourceUrl } from '@angular/platform-browser';
import { OnlyOfficeViewer } from '../../components/cards/onlyoffice-viewer/onlyoffice-viewer';
import { FichierService } from '../../services/fichier-service';
import { EmailList } from '../../components/lists/email-list/email-list';


// Interface pour le formulaire d'activité avec emails sous forme de string
interface ActiviteForm {
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  emailsString: string;
}

interface TacheForm {
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  emailsString: string;
}

@Component({
  selector: 'app-project-detail',
  templateUrl: './project-detail.html',
  styleUrls: ['./project-detail.css'],
  imports:[MaterialModule,FileList,FormsModule,PdfModal,OnlyOfficeViewer,EmailList]
})
export class ProjectDetail implements OnInit {
  projetId!: number;
  projet?: Projet;
  activites: Activite[] = [];
  loading = true;
  currentDocumentId?: number;
  // Gestion des fichiers temporaires
  selectedFilesActivite: File[] = [];
  selectedFilesTache: File[] = [];

  // Gestion du formulaire activité
  showActiviteForm = false;
  editingActivite?: Activite;
  activiteForm: ActiviteForm = {
    designation: '',
    description: '',
    dateDebut: new Date(),
    dateFin: new Date(),
    status: 'EN_ATTENTE',
    emailsString: ''
  };

  // Gestion du formulaire tâche
  showTacheFormForActiviteId?: number;
  editingTache?: Tache;
  tacheForm: TacheForm = {
    designation: '',
    description: '',
    dateDebut: new Date(),
    dateFin: new Date(),
    status: 'EN_ATTENTE',
    emailsString: ''
  };

  // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  fichierInfo: FichierInfo | null = null;

  // Utilisateur connecté (à remplacer par votre service)
  private currentUserId = 0; // exemple

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private activiteService: ActiviteService,
    private tacheService: TacheService,
    private authService: AuthService, // à injecter si disponible
    private dialog: MatDialog,
    private fichierService:FichierService
  ) {
  
  }

  ngOnInit(): void {
    this.projetId = +this.route.snapshot.paramMap.get('id')!;
    this.loadProjet();
    this.loadActivites();
      this.authService.user$.subscribe((user)=>{
        this.currentUserId=user?.id||0;
    })
  }

  goBack(): void {
    this.router.navigate(['/app/projects']);
  }

  loadProjet(): void {
    this.projetService.getProjetById(this.projetId).subscribe({
      next: (data) => {
        this.projet = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement projet', err);
        this.loading = false;
      }
    });
  }

  loadActivites(): void {
    this.activiteService.getByProjet(this.projetId).subscribe({
      next: (data) => {
        this.activites = data;
        this.activites.forEach(act => this.loadTachesForActivite(act));
      },
      error: (err) => console.error('Erreur chargement activités', err)
    });
  }

  loadTachesForActivite(activite: Activite): void {
    this.tacheService.getByActivite(activite.id).subscribe({
      next: (taches) => activite.taches = taches,
      error: (err) => console.error('Erreur chargement tâches', err)
    });
  }

  // ---------- Gestion des fichiers sélectionnés ----------
  onFilesSelected(event: Event, type: 'activite' | 'tache'): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (type === 'activite') {
        this.selectedFilesActivite = files;
      } else {
        this.selectedFilesTache = files;
      }
    }
  }

  // ---------- Activités ----------
  onCreateActivite(): void {
    this.showActiviteForm = true;
    this.editingActivite = undefined;
    this.activiteForm = {
      designation: '',
      description: '',
      dateDebut: new Date(),
      dateFin: new Date(),
      status: 'EN_ATTENTE',
      emailsString: ''
    };
    this.selectedFilesActivite = [];
  }

  onEditActivite(activite: Activite): void {
    this.showActiviteForm = true;
    this.editingActivite = activite;
    // Remplir le formulaire avec les données existantes
    this.activiteForm = {
      designation: activite.designation,
      description: activite.description || '',
      dateDebut: new Date(activite.dateDebut),
      dateFin: new Date(activite.dateFin),
      status: activite.status,
      emailsString: activite.emails?.map(e => e.email).join(', ') || ''
    };
    this.selectedFilesActivite = []; // on ne recharge pas les fichiers existants ici
  }

  onDeleteActivite(activite: Activite): void {
    if (confirm(`Supprimer l'activité "${activite.designation}" ?`)) {
      this.activiteService.delete(activite.id).subscribe({
        next: () => {
          this.activites = this.activites.filter(a => a.id !== activite.id);
        },
        error: (err) => console.error('Erreur suppression', err)
      });
    }
  }

  onSaveActivite(): void {
    // Construction du DTO à partir du formulaire
    const dto: CreateActiviteDto | UpdateActiviteDto = {
      designation: this.activiteForm.designation,
      description: this.activiteForm.description || undefined,
      dateDebut: this.activiteForm.dateDebut,
      dateFin: this.activiteForm.dateFin,
      status: this.activiteForm.status,
      createurId:this.currentUserId,
      emails: this.parseEmails(this.activiteForm.emailsString)
    };

    if (this.editingActivite) {
      // Mise à jour
      this.activiteService.update(this.editingActivite.id, dto as UpdateActiviteDto).subscribe({
        next: (updated) => {
          const index = this.activites.findIndex(a => a.id === updated.id);
          if (index !== -1) this.activites[index] = updated;
          this.cancelActiviteForm();
        },
        error: (err) => console.error('Erreur mise à jour', err)
      });
    } else {
      // Création avec fichiers
      (dto as CreateActiviteDto).createurId = this.currentUserId;
      this.activiteService.create(this.projetId, dto as CreateActiviteDto, this.selectedFilesActivite).subscribe({
        next: (created) => {
          this.activites.push(created);
          this.cancelActiviteForm();
        },
        error: (err) => console.error('Erreur création', err)
      });
    }
  }

  cancelActiviteForm(): void {
    this.showActiviteForm = false;
    this.editingActivite = undefined;
    this.activiteForm = {} as ActiviteForm;
    this.selectedFilesActivite = [];
  }

  // ---------- Tâches ----------
  onAddTache(activiteId: number): void {
    this.showTacheFormForActiviteId = activiteId;
    this.editingTache = undefined;
    this.tacheForm = {
      designation: '',
      description: '',
      dateDebut: new Date(),
      dateFin: new Date(),
      status: 'EN_ATTENTE',
      emailsString: ''
    };
    this.selectedFilesTache = [];
  }

  onEditTache(tache: Tache): void {
    this.showTacheFormForActiviteId = tache.activiteId;
    this.editingTache = tache;
    this.tacheForm = {
      designation: tache.designation,
      description: tache.description || '',
      dateDebut: new Date(tache.dateDebut),
      dateFin: new Date(tache.dateFin),
      status: tache.status,
      emailsString: tache.emails?.map(e => e.email).join(', ') || ''
    };
    this.selectedFilesTache = [];
  }

  onDeleteTache(tache: Tache): void {
    if (confirm(`Supprimer la tâche "${tache.designation}" ?`)) {
      this.tacheService.delete(tache.id).subscribe({
        next: () => {
          const activite = this.activites.find(a => a.id === tache.activiteId);
          if (activite && activite.taches) {
            activite.taches = activite.taches.filter(t => t.id !== tache.id);
          }
        },
        error: (err) => console.error('Erreur suppression tâche', err)
      });
    }
  }

  onSaveTache(): void {
    const dto: CreateTacheDto | UpdateTacheDto = {
      designation: this.tacheForm.designation,
      description: this.tacheForm.description || undefined,
      dateDebut: this.tacheForm.dateDebut,
      dateFin: this.tacheForm.dateFin,
      status: this.tacheForm.status,
      emails: this.parseEmails(this.tacheForm.emailsString)
    };

    if (this.editingTache) {
      this.tacheService.update(this.editingTache.id, dto as UpdateTacheDto).subscribe({
        next: (updated) => {
          const activite = this.activites.find(a => a.id === updated.activiteId);
          if (activite && activite.taches) {
            const index = activite.taches.findIndex(t => t.id === updated.id);
            if (index !== -1) activite.taches[index] = updated;
          }
          this.cancelTacheForm();
        },
        error: (err) => console.error('Erreur mise à jour tâche', err)
      });
    } else {
      (dto as CreateTacheDto).createurId = this.currentUserId;
      this.tacheService.create(this.showTacheFormForActiviteId!, dto as CreateTacheDto, this.selectedFilesTache).subscribe({
        next: (created) => {
          const activite = this.activites.find(a => a.id === created.activiteId);
          if (activite) {
            if (!activite.taches) activite.taches = [];
            activite.taches.push(created);
          }
          this.cancelTacheForm();
        },
        error: (err) => console.error('Erreur création tâche', err)
      });
    }
  }

  cancelTacheForm(): void {
    this.showTacheFormForActiviteId = undefined;
    this.editingTache = undefined;
    this.tacheForm = {} as TacheForm;
    this.selectedFilesTache = [];
  }

  // ---------- Gestion des fichiers (visualisation/suppression) ----------
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
  this.fichierInfo=file;
  this.fileName = file.nomFichier;
  this.currentDocumentId = file.id;
  this.isOnlyOfficeModalOpen = true;
}

  }
  onViewerError(error: string): void {
  console.error('Erreur du viewer ONLYOFFICE:', error);
  // Afficher une notification à l'utilisateur
}

  onDeleteFile(file: FichierInfo, parentType: 'projet' | 'activite' | 'tache', parentId: number): void {
    if (confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) {
      let deleteObservable: Observable<any>;
      switch (parentType) {
        case 'projet':
          deleteObservable = this.projetService.deleteFichier(parentId, file.id);
          break;
        case 'activite':
          deleteObservable = this.activiteService.deleteFichier(parentId, file.id);
          break;
        case 'tache':
          deleteObservable = this.tacheService.deleteFichier(parentId, file.id);
          break;
        default:
          return;
      }
      deleteObservable.subscribe({
        next: () => {
          // Mise à jour locale : retirer le fichier de la liste du parent concerné
          // Pour simplifier, on recharge les activités (ou on peut faire une mise à jour plus fine)
          this.loadActivites();
        },
        error: (err) => console.error('Erreur suppression fichier', err)
      });
    }
  }

  // Fermeture des modals
  onPdfModalClosed(): void {
    this.isPdfModalOpen = false;
    this.pdfPreviewUrl = '';
  }

  onOnlyOfficeModalClosed(): void {
    this.isOnlyOfficeModalOpen = false;
    this.onlyOfficeFileUrl = null;
    this.fichierInfo=null;
  }

  // ---------- Utilitaires ----------
  private parseEmails(emailsString: string): string[] {
    if (!emailsString) return [];
    return emailsString.split(',').map(email => (email.trim() )).filter(e => e);
  }

  onEditEmail(email: EmailDto): void {
    console.log('Éditer email', email);
    // Implémenter l'édition d'email (par exemple avec un dialog)
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

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      EN_ATTENTE: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      EN_COURS: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      TERMINE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      ANNULE: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    };
    return colors[status] || 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
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
onEmailAddedToActivite(email: EmailDto, activite: Activite) {
  if (!activite.emails) {
    activite.emails = [];
  }
  activite.emails.push(email);
}

onEmailUpdatedInActivite(email: EmailDto, activite: Activite) {
  const index = activite.emails?.findIndex(e => e.id === email.id) ?? -1;
  if (index !== -1) {
    activite.emails![index] = email;
  }
}

onEmailDeletedFromActivite(id: number, activite: Activite) {
  activite.emails = activite.emails?.filter(e => e.id !== id) ?? [];
}
onEmailAddedToTache(email: EmailDto, tache: Tache) {
  if (!tache.emails) {
    tache.emails = [];
  }
  tache.emails.push(email);
}

onEmailUpdatedInTache(email: EmailDto, tache: Tache) {
  const index = tache.emails?.findIndex(e => e.id === email.id) ?? -1;
  if (index !== -1) {
    tache.emails![index] = email;
  }
}

onEmailDeletedFromTache(id: number, tache: Tache) {
  tache.emails = tache.emails?.filter(e => e.id !== id) ?? [];
}

}