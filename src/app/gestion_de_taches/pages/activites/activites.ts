// src/app/gestion_de_taches/pages/activites/activites.component.ts
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { DateDto, EditableActivieDto, EditableDto, EmailDto, FichierInfo } from '../../interfaces/base-entity-gestion';

import { AuthService } from '../../services/AuthService';

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
import { ActiviteService } from '../../services/activite.service';

import { User } from '../../models/user';
import { ActiviteForm } from '../../components/forms/activite-form/activite-form';
import { ActiviteCard } from '../../components/cards/activite-card/activite-card';
import { Activite } from '../../models/activite.model';
import { Page } from '../../interfaces/generals';

@Component({
  selector: 'app-activites',
  templateUrl: './activites.html',
  imports: [MaterialModule, FormsModule, CommonModule, ActiviteCard, PdfModal, MatPaginator],
  styleUrls: ['./activites.css']
})
export class Activites implements OnInit {
  activites: Activite[] = [];
  filteredActivites: Activite[] = [];
  loading: boolean = false;
  searchTerm = '';
  selectedStatus: string = 'TOUS';
  statusOptions = ['TOUS', 'EN_ATTENTE', 'EN_COURS', 'TERMINE', 'ANNULE'];
  private searchSubject = new Subject<string>();

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 25, 50];

  // Visualisation
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl | string = '';
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  currentDocumentId?: number;
  user = signal<User | null>(null);

  constructor(
    private activiteService: ActiviteService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private emailService: EmailService,
    private fichierService: FichierService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.user.set(user);
    });
    this.loadActivites();
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      // Reset to first page on search
      this.currentPage = 0;
      this.loadActivites();
    });
  }

  loadActivites(page: number = this.currentPage): void {
    this.loading = true;

    this.activiteService
      .getMesActivites(this.user()?.email || '', page, this.pageSize)
      .pipe(
        catchError((error) => {
          console.error('Erreur chargement activites:', error);
          this.snackBar.open(
            error.status === 404
              ? 'Aucune activité trouvée'
              : 'Erreur lors du chargement des activités',
            'Fermer',
            { duration: 3000 }
          );
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: 0,
            size: this.pageSize,
            first: true,
            last: true
          } as Page<Activite>);
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe((pageResult) => {
        this.activites = pageResult.content;
        this.totalElements = pageResult.totalElements;
        this.totalPages = pageResult.totalPages;
        this.currentPage = pageResult.number;
        this.filterActivites();
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadActivites(event.pageIndex);
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadActivites();
  }

  filterActivites(): void {
    let filtered = [...this.activites];

    if (this.searchTerm) {
      filtered = filtered.filter(p =>
        p.designation.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.description?.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.selectedStatus !== 'TOUS') {
      filtered = filtered.filter(p => p.status === this.selectedStatus);
    }

    this.filteredActivites = filtered;
  }

  onApplieELementChange(editableChamps: EditableActivieDto) {
    let observable;
    let messageSuccess = '';
    let messageError = '';

    switch (editableChamps.type) {
      case 'DESIGNATION':
        observable = this.activiteService.updateActiviteDesignation(
          editableChamps.activite.id,
          { texte: editableChamps.texte }
        );
        messageSuccess = 'Désignation mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la désignation.';
        break;

      case 'DESCRIPTION':
        observable = this.activiteService.updateActiviteDescription(
          editableChamps.activite.id,
          { texte: editableChamps.texte }
        );
        messageSuccess = 'Description mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la description.';
        break;

      case 'DATE_DEBUT':
        observable = this.activiteService.updateActiviteDateDebut(
          editableChamps.activite.id,
          { date: editableChamps.date || new Date() }
        );
        messageSuccess = 'Date de début mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la date de début.';
        break;

      case 'DATE_FIN':
        observable = this.activiteService.updateActiviteDateFin(
          editableChamps.activite.id,
          { date: editableChamps.date || new Date() }
        );
        messageSuccess = 'Date de fin mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la date de fin.';
        break;

      default:
        return;
    }

    observable.subscribe({
      next: (activiteMisAJour) => {
        this.updateActivite(this.activites, activiteMisAJour);
        this.updateActivite(this.filteredActivites, activiteMisAJour);
        this.notificationService.success(messageSuccess);
      },
      error: (err) => {
        console.error(err);
        this.notificationService.error(messageError);
      }
    });
  }

  updateActivite(liste: Activite[], activiteMisAJour: Activite) {
    const index = liste.findIndex(p => p.id === activiteMisAJour.id);
    if (index !== -1) {
      liste[index] = activiteMisAJour;
    }
  }

  onAddFiles(activite: Activite, files: File[]): void {
    this.fichierService.uploadFiles('ACTIVITE', activite.id, files).pipe(
      switchMap(() => this.fichierService.listFiles('ACTIVITE', activite.id))
    ).subscribe({
      next: (fichiers) => {
        const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
          id: f.id,
          nomFichier: f.nomFichier ?? '',
          url: f.url ?? '',
          type: f.type ?? '',
          callbackurl: f.callbackurl ?? ''
        }));

        const updateActivite = (list: Activite[]) => {
          const idx = list.findIndex(p => p.id === activite.id);
          if (idx !== -1) {
            list[idx] = Object.assign({}, list[idx], { fichiers: updatedFichiers });
          }
        };

        updateActivite(this.activites);
        updateActivite(this.filteredActivites);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.snackBar.open("Erreur lors de l'upload", 'Fermer', { duration: 3000 });
      }
    });
  }

  onDeleteFile(activite: Activite, fichier: FichierInfo): void {
    this.fichierService.deleteFile('ACTIVITE', activite.id, fichier.id).subscribe({
      next: () => {
        const removeFile = (list: Activite[]) => {
          const idx = list.findIndex(p => p.id === activite.id);
          if (idx !== -1) {
            list[idx] = Object.assign({}, list[idx], {
              fichiers: list[idx].fichiers?.filter(f => f.id !== fichier.id) ?? []
            });
          }
        };

        removeFile(this.activites);
        removeFile(this.filteredActivites);
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
    this.currentPage = 0;
    this.loadActivites();
  }

  getSimpleEmails(activite: Activite): string {
    return activite.emails?.map(e => e.email).join(', ') ?? '';
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ActiviteForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActivites();
        this.snackBar.open('Activite créé avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openEditDialog(activite: Activite): void {
    const dialogRef = this.dialog.open(ActiviteForm, {
      width: '600px',
      data: { mode: 'edit', activite, projetId: undefined }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActivites();
        this.snackBar.open('Activite mis à jour avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openDeleteDialog(activite: Activite): void {
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      width: '400px',
      data: {
        title: 'Supprimer le activite',
        message: `Êtes-vous sûr de vouloir supprimer le activite "${activite.designation}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteActivite(activite.id);
      }
    });
  }

  deleteActivite(id: number): void {
    this.activiteService.delete(id).subscribe({
      next: () => {
        // Si on supprime le dernier élément de la page courante, revenir à la page précédente
        if (this.activites.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.loadActivites();
        this.snackBar.open('Activite supprimé avec succès', 'Fermer', { duration: 3000 });
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

  onEditEmail(email: EmailDto): void {
    const dialogRef = this.dialog.open(EditEmailDialog, {
      width: '400px',
      data: { email, emailType: 'EMAIL_ACTIVITE' } as EditEmailDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadActivites();
      }
    });
  }
}