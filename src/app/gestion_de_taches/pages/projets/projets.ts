// src/app/gestion_de_taches/pages/projets/projets.component.ts
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DateDto, EditableDto, EmailDto, FichierInfo, Projet } from '../../interfaces/base-entity-gestion';
import { ProjetService } from '../../services/projet-service';
import { AuthService } from '../../services/AuthService';
import { ProjetForm } from '../../components/projet-form/projet-form';
import { ConfirmationDialog } from '../../components/confirmation-dialog/confirmation-dialog';
import { MaterialModule } from '../../material.module';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { catchError, debounceTime, distinctUntilChanged, finalize, of, Subject, switchMap } from 'rxjs';
import { EmailService } from '../../services/emails/email-service';
import { EmailCard } from '../../components/cards/email-card/email-card';
import { EditEmailDialog, EditEmailDialogData } from '../../components/cards/edit-email-dialog';
import { ProjectCard } from '../../components/cards/project-card/project-card';
import { SafeResourceUrl } from '@angular/platform-browser';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { FichierService } from '../../services/fichier-service';
import { TexteDto } from '../../interfaces/base-entity-gestion';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-projets',
  templateUrl: './projets.html',
  imports:[MaterialModule,FormsModule,CommonModule,ProjectCard,PdfModal],
  styleUrls: ['./projets.css']
})
export class Projets implements OnInit {
  projets: Projet[] = [];
  filteredProjets: Projet[] = [];
  loading:boolean = false;
  searchTerm = '';
  selectedStatus: string = 'TOUS';
  statusOptions = ['TOUS', 'EN_ATTENTE', 'EN_COURS', 'TERMINE', 'ANNULE'];
   private searchSubject = new Subject<string>();
     // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl|string  ='';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  currentDocumentId?: number;
  constructor(
    private projetService: ProjetService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
     private cdr: ChangeDetectorRef,
       private emailService: EmailService,
       private fichierService:FichierService,
       private notificationService: NotificationService
  ) {}

ngOnInit(): void {
  this.loadProjets();
  this.searchSubject.pipe(
    debounceTime(300),
    distinctUntilChanged()
  ).subscribe(() => {
    this.filterProjets();
  });
}


loadProjets(): void {
  this.loading = true;

  this.projetService.getMesProjets()
    .pipe(
      catchError((error) => {
        console.error('Erreur chargement projets:', error);
        this.snackBar.open(
          error.status === 404 ? 'Aucun projet trouvé' : 'Erreur lors du chargement des projets',
          'Fermer',
          { duration: 3000 }
        );
        // Retourne un tableau vide pour que la subscription continue
        return of([]);
      }),
      finalize(() => {
        this.loading = false; // seul endroit pour masquer le spinner
        this.cdr.detectChanges()
      })
    )
    .subscribe((projets) => {
      this.projets = Array.isArray(projets) ? projets : [];
      this.filterProjets();
    });
}
onApplieELementChange(editableChamps: EditableDto) {
  let observable;
  let messageSuccess = '';
  let messageError = '';

  switch (editableChamps.type) {
    case 'DESIGNATION':
      observable = this.projetService.updateProjetDesignation(
        editableChamps.projet.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Désignation mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la désignation.';
      break;

    case 'DESCRIPTION':
      observable = this.projetService.updateProjetDescription(
        editableChamps.projet.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Description mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la description.';
      break;

    case 'DATE_DEBUT':
      observable = this.projetService.updateProjetDateDebut(
        editableChamps.projet.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de début mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de début.';
      break;

    case 'DATE_FIN':
      observable = this.projetService.updateProjetDateFin(
        editableChamps.projet.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de fin mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de fin.';
      break;

    default:
      return;
  }

  observable.subscribe({
    next: (projetMisAJour) => {
      // Mettre à jour le projet dans la liste principale
      this.updateProjet(this.projets, projetMisAJour);

      // Mettre à jour le projet dans la liste filtrée
      this.updateProjet(this.filteredProjets, projetMisAJour);

      this.notificationService.success(messageSuccess)
    },
    error: (err) => {
      console.error(err);
     
      this.notificationService.error(messageError)
    }
  });
}

/**
 * Remplace le projet existant dans la liste par le projet mis à jour
 */
updateProjet(liste: Projet[], projetMisAJour: Projet) {
  const index = liste.findIndex(p => p.id === projetMisAJour.id);
  if (index !== -1) {
    liste[index] = projetMisAJour;
  }
}
// Remplacer onAddFiles par cette version
onAddFiles(projet: Projet, files: File[]): void {
  this.fichierService.uploadFiles('PROJET', projet.id, files).pipe(
    switchMap(() => this.fichierService.listFiles('PROJET', projet.id))
  ).subscribe({
    next: (fichiers) => {
      const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
        id: f.id,
        nomFichier: f.nomFichier ?? '',
        url: f.url ?? '',
        type: f.type ?? '',
        callbackurl:f.callbackurl??''
      }));

    const updateProjet = (list: Projet[]) => {
  const idx = list.findIndex(p => p.id === projet.id);
  if (idx !== -1) {
    // Object.assign préserve l'instance de classe (et donc le getter simpleEmails)
    list[idx] = Object.assign(new Projet(), list[idx], { fichiers: updatedFichiers });
  }
};

      updateProjet(this.projets);
      updateProjet(this.filteredProjets);
      this.cdr.detectChanges();
    },
    error: (err) => {
      console.error('Erreur upload/récupération fichiers:', err);
      this.snackBar.open("Erreur lors de l'upload", 'Fermer', { duration: 3000 });
    }
  });
}

// Ajouter cette méthode pour la suppression sans recharger toute la page
onDeleteFile(projet: Projet, fichier: FichierInfo): void {
  this.fichierService.deleteFile('PROJET', projet.id, fichier.id).subscribe({
    next: () => {
      const removeFile = (list: Projet[]) => {
        const idx = list.findIndex(p => p.id === projet.id);
        if (idx !== -1) {
          list[idx] = Object.assign(new Projet(), list[idx], {
            fichiers: list[idx].fichiers?.filter(f => f.id !== fichier.id) ?? []
          });
        }
      };

      removeFile(this.projets);
      removeFile(this.filteredProjets);
      this.cdr.detectChanges();

      this.snackBar.open('Fichier supprimé', 'Fermer', { duration: 2000 });
    },
    error: (err) => {
      console.error('Erreur suppression fichier:', err);
      this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
    }
  });
}
  resetFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = 'TOUS';
    this.filteredProjets = [...this.projets];
  }
getSimpleEmails(projet: Projet): string {
  
  return projet.emails?.map(e => e.email).join(', ') ?? '';
}
  filterProjets(): void {
    let filtered = [...this.projets];
    
    if (this.searchTerm) {
      filtered = filtered.filter(p => 
        p.designation.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.description?.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }
    
    if (this.selectedStatus !== 'TOUS') {
      filtered = filtered.filter(p => p.status === this.selectedStatus);
    }
    
    this.filteredProjets = filtered;
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ProjetForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProjets();
        this.snackBar.open('Projet créé avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openEditDialog(projet: Projet): void {
    const dialogRef = this.dialog.open(ProjetForm, {
      width: '600px',
   
      data: { mode: 'edit', projet }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProjets();
        this.snackBar.open('Projet mis à jour avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openDeleteDialog(projet: Projet): void {
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      width: '400px',
      data: {
        title: 'Supprimer le projet',
        message: `Êtes-vous sûr de vouloir supprimer le projet "${projet.designation}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteProjet(projet.id);
      }
    });
  }

  deleteProjet(id: number): void {
    this.projetService.deleteProjet(id).subscribe({
      next: () => {
        this.loadProjets();
        this.snackBar.open('Projet supprimé avec succès', 'Fermer', { duration: 3000 });
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      }
    });
  }

  getStatusColor(status: string): string {
    const colors = {
      'EN_ATTENTE': 'bg-yellow-100 text-yellow-800',
      'EN_COURS': 'bg-blue-100 text-blue-800',
      'TERMINE': 'bg-green-100 text-green-800',
      'ANNULE': 'bg-red-100 text-red-800'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      'EN_ATTENTE': 'En attente',
      'EN_COURS': 'En cours',
      'TERMINE': 'Terminé',
      'ANNULE': 'Annulé'
    };
    return labels[status as keyof typeof labels] || status;
  }

  onSearch(): void {
    this.filterProjets();
  }

  onStatusChange(): void {
    this.filterProjets();
  }

 onEditEmail(email: EmailDto): void {
  const dialogRef = this.dialog.open(EditEmailDialog, {
    width: '400px',
    data: { email, emailType: 'EMAIL_PROJET' } as EditEmailDialogData
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      // Le dialogue a retourné le nouvel email, on recharge la liste
      this.loadProjets();
    }
  });
}
}