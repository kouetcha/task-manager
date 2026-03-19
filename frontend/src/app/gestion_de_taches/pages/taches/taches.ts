// src/app/gestion_de_taches/pages/taches/taches.component.ts
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { EditableTacheDto,  EmailDto, FichierInfo } from '../../interfaces/base-entity-gestion';

import { AuthService } from '../../services/AuthService';

import { ConfirmationDialog } from '../../components/confirmation-dialog/confirmation-dialog';
import { MaterialModule } from '../../material.module';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { catchError, debounceTime, distinctUntilChanged, finalize, of, Subject, switchMap } from 'rxjs';
import { EmailService } from '../../services/emails/email-service';

import { EditEmailDialog, EditEmailDialogData } from '../../components/cards/edit-email-dialog';

import { SafeResourceUrl } from '@angular/platform-browser';
import { FichierService } from '../../services/fichier-service';

import { NotificationService } from '../../services/notification.service';
import { TacheService } from '../../services/tache.service';

import { User } from '../../models/user';
import { TacheForm } from '../../components/forms/tache-form/tache-form';
import { TacheCard } from '../../components/cards/tache-card/tache-card';
import { Tache } from '../../models/tache.model';
import { Page } from '../../interfaces/generals';

@Component({
  selector: 'app-taches',
  templateUrl: './taches.html',
  imports: [MaterialModule, FormsModule, CommonModule, TacheCard,  MatPaginator],
  styleUrls: ['./taches.css']
})
export class Taches implements OnInit {
  taches: Tache[] = [];
  filteredTaches: Tache[] = [];
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
    private tacheService: TacheService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private fichierService: FichierService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.user.set(user);
    });
    this.loadTaches();
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      // Reset to first page on search
      this.currentPage = 0;
      this.loadTaches();
    });
  }

  loadTaches(page: number = this.currentPage): void {
    this.loading = true;

    this.tacheService
      .getMesTaches(this.user()?.email || '', page, this.pageSize)
      .pipe(
        catchError((error) => {
          console.error('Erreur chargement taches:', error);
          this.snackBar.open(
            error.status === 404
              ? 'Aucune activité trouvée'
              : 'Erreur lors du chargement des activités',
            'Fermer',
            { duration: 3000 }
          );
          return of({
            content: [],
            page:{totalElements: 0,
            totalPages: 0,
            number: 0,
            size: this.pageSize},
            first: true,
            last: true
          } as Page<Tache>);
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe((pageResult) => {
        this.taches = pageResult.content;
        this.totalElements = pageResult.page.totalElements;
        this.totalPages = pageResult.page.totalPages;
        this.currentPage = pageResult.page.number;
        this.filterTaches();
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadTaches(event.pageIndex);
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadTaches();
  }

  filterTaches(): void {
    let filtered = [...this.taches];

    if (this.searchTerm) {
      filtered = filtered.filter(p =>
        p.designation.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        p.description?.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    if (this.selectedStatus !== 'TOUS') {
      filtered = filtered.filter(p => p.status === this.selectedStatus);
    }

    this.filteredTaches = filtered;
  }

  onApplieELementChange(editableChamps: EditableTacheDto) {
    let observable;
    let messageSuccess = '';
    let messageError = '';

    switch (editableChamps.type) {
      case 'DESIGNATION':
        observable = this.tacheService.updateTacheDesignation(
          editableChamps.tache.id,
          { texte: editableChamps.texte }
        );
        messageSuccess = 'Désignation mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la désignation.';
        break;

      case 'DESCRIPTION':
        observable = this.tacheService.updateTacheDescription(
          editableChamps.tache.id,
          { texte: editableChamps.texte }
        );
        messageSuccess = 'Description mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la description.';
        break;

      case 'DATE_DEBUT':
        observable = this.tacheService.updateTacheDateDebut(
          editableChamps.tache.id,
          { date: editableChamps.date || new Date() }
        );
        messageSuccess = 'Date de début mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la date de début.';
        break;

      case 'DATE_FIN':
        observable = this.tacheService.updateTacheDateFin(
          editableChamps.tache.id,
          { date: editableChamps.date || new Date() }
        );
        messageSuccess = 'Date de fin mise à jour avec succès !';
        messageError = 'Erreur lors de la mise à jour de la date de fin.';
        break;

      default:
        return;
    }

    observable.subscribe({
      next: (tacheMisAJour) => {
        this.updateTache(this.taches, tacheMisAJour);
        this.updateTache(this.filteredTaches, tacheMisAJour);
        this.notificationService.success(messageSuccess);
      },
      error: (err) => {
        console.error(err);
        this.notificationService.error(messageError);
      }
    });
  }

  updateTache(liste: Tache[], tacheMisAJour: Tache) {
    const index = liste.findIndex(p => p.id === tacheMisAJour.id);
    if (index !== -1) {
      liste[index] = tacheMisAJour;
    }
  }

  onAddFiles(tache: Tache, files: File[]): void {
    this.fichierService.uploadFiles('ACTIVITE', tache.id, files).pipe(
      switchMap(() => this.fichierService.listFiles('ACTIVITE', tache.id))
    ).subscribe({
      next: (fichiers) => {
        const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
          id: f.id,
          nomFichier: f.nomFichier ?? '',
          url: f.url ?? '',
          type: f.type ?? '',
          callbackurl: f.callbackurl ?? ''
        }));

        const updateTache = (list: Tache[]) => {
          const idx = list.findIndex(p => p.id === tache.id);
          if (idx !== -1) {
            list[idx] = Object.assign({}, list[idx], { fichiers: updatedFichiers });
          }
        };

        updateTache(this.taches);
        updateTache(this.filteredTaches);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.snackBar.open("Erreur lors de l'upload", 'Fermer', { duration: 3000 });
      }
    });
  }

  onDeleteFile(tache: Tache, fichier: FichierInfo): void {
    this.fichierService.deleteFile('ACTIVITE', tache.id, fichier.id).subscribe({
      next: () => {
        const removeFile = (list: Tache[]) => {
          const idx = list.findIndex(p => p.id === tache.id);
          if (idx !== -1) {
            list[idx] = Object.assign({}, list[idx], {
              fichiers: list[idx].fichiers?.filter(f => f.id !== fichier.id) ?? []
            });
          }
        };

        removeFile(this.taches);
        removeFile(this.filteredTaches);
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
    this.loadTaches();
  }

  getSimpleEmails(tache: Tache): string {
    return tache.emails?.map(e => e.email).join(', ') ?? '';
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(TacheForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTaches();
        this.snackBar.open('Tache créé avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openEditDialog(tache: Tache): void {
    const dialogRef = this.dialog.open(TacheForm, {
      width: '600px',
      data: { mode: 'edit', tache, projetId: undefined }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTaches();
        this.snackBar.open('Tache mis à jour avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openDeleteDialog(tache: Tache): void {
    const dialogRef = this.dialog.open(ConfirmationDialog, {
      width: '400px',
      data: {
        title: 'Supprimer le tache',
        message: `Êtes-vous sûr de vouloir supprimer le tache "${tache.designation}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteTache(tache.id);
      }
    });
  }

  deleteTache(id: number): void {
    this.tacheService.delete(id).subscribe({
      next: () => {
        // Si on supprime le dernier élément de la page courante, revenir à la page précédente
        if (this.taches.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.loadTaches();
        this.snackBar.open('Tache supprimé avec succès', 'Fermer', { duration: 3000 });
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
        this.loadTaches();
      }
    });
  }
}