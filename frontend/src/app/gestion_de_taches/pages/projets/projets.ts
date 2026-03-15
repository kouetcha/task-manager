// src/app/gestion_de_taches/pages/projets/projets.component.ts
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DateDto, EditableDto, EmailDto, FichierInfo, Projet } from '../../interfaces/base-entity-gestion';
import { ProjetService } from '../../services/projet-service';
import { AuthService } from '../../services/AuthService';
import { ProjetForm } from '../../components/projet-form/projet-form';
import { ConfirmationDialog } from '../../components/confirmation-dialog/confirmation-dialog';
import { MaterialModule } from '../../material.module';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { catchError, debounceTime, distinctUntilChanged, finalize, of, Subject, switchMap } from 'rxjs';
import { EditEmailDialog, EditEmailDialogData } from '../../components/cards/edit-email-dialog';
import { ProjectCard } from '../../components/cards/project-card/project-card';
import { SafeResourceUrl } from '@angular/platform-browser';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { FichierService } from '../../services/fichier-service';
import { NotificationService } from '../../services/notification.service';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Page } from '../../interfaces/generals';
import { User } from '../../models/user';

@Component({
  selector: 'app-projets',
  templateUrl: './projets.html',
  imports: [MaterialModule, FormsModule, CommonModule, ProjectCard, MatPaginator],
  styleUrls: ['./projets.css']
})
export class Projets implements OnInit {
  projets: Projet[] = [];
  filteredProjets: Projet[] = [];
  loading = false;
  searchTerm = '';
  selectedStatus = 'TOUS';
  statusOptions = ['TOUS', 'EN_ATTENTE', 'EN_COURS', 'TERMINE', 'ANNULE'];
  private searchSubject = new Subject<string>();

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 25, 50];

  user = signal<User | null>(null);

  constructor(
    private projetService: ProjetService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private fichierService: FichierService,
    private notificationService: NotificationService
  ) {
    this.authService.user$.subscribe(user => this.user.set(user));
  }

  ngOnInit(): void {
    

    this.loadProjets();

    // Debounce sur la recherche — remet à la page 0 avant de recharger
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadProjets();
    });
  }

  // ─── Chargement ──────────────────────────────────────────────────────────────

  loadProjets(page: number = this.currentPage): void {
    this.loading = true;

    this.projetService
      .getMesProjets(this.user()?.email || '', page, this.pageSize)
      .pipe(
        catchError((error) => {
          console.error('Erreur chargement projets:', error);
          this.snackBar.open(
            error.status === 404
              ? 'Aucun projet trouvé'
              : 'Erreur lors du chargement des projets',
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
            last: true,
          } as Page<Projet>);
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe((pageResult) => {
        this.projets = pageResult.content;
        this.totalElements = pageResult.totalElements;
        this.totalPages = pageResult.totalPages;
        this.currentPage = pageResult.number;
        this.filterProjets();
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadProjets(event.pageIndex);
  }

  // ─── Filtres ─────────────────────────────────────────────────────────────────

  filterProjets(): void {
    let filtered = [...this.projets];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(p =>
        p.designation.toLowerCase().includes(term) ||
        p.description?.toLowerCase().includes(term)
      );
    }

    if (this.selectedStatus !== 'TOUS') {
      filtered = filtered.filter(p => p.status === this.selectedStatus);
    }

    this.filteredProjets = filtered;
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadProjets();
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = 'TOUS';
    this.currentPage = 0;
    this.loadProjets();
  }

  // ─── Édition inline ──────────────────────────────────────────────────────────

  onApplieELementChange(editableChamps: EditableDto): void {
    type CaseType = 'DESIGNATION' | 'DESCRIPTION' | 'DATE_DEBUT' | 'DATE_FIN';

    const config: Record<CaseType, { observable: any; success: string; error: string }> = {
      DESIGNATION: {
        observable: this.projetService.updateProjetDesignation(
          editableChamps.projet.id, { texte: editableChamps.texte }
        ),
        success: 'Désignation mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la désignation.',
      },
      DESCRIPTION: {
        observable: this.projetService.updateProjetDescription(
          editableChamps.projet.id, { texte: editableChamps.texte }
        ),
        success: 'Description mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la description.',
      },
      DATE_DEBUT: {
        observable: this.projetService.updateProjetDateDebut(
          editableChamps.projet.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de début mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de début.',
      },
      DATE_FIN: {
        observable: this.projetService.updateProjetDateFin(
          editableChamps.projet.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de fin mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de fin.',
      },
    };

    const action = config[editableChamps.type as CaseType];
    if (!action) return;

    action.observable.subscribe({
      next: (projetMisAJour: Projet) => {
        this.updateProjet(this.projets, projetMisAJour);
        this.updateProjet(this.filteredProjets, projetMisAJour);
        this.notificationService.success(action.success);
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error(err);
        this.notificationService.error(action.error);
      },
    });
  }

  updateProjet(liste: Projet[], projetMisAJour: Projet): void {
    const index = liste.findIndex(p => p.id === projetMisAJour.id);
    if (index !== -1) liste[index] = projetMisAJour;
  }

  // ─── Fichiers ────────────────────────────────────────────────────────────────

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
          callbackurl: f.callbackurl ?? '',
        }));

        const applyUpdate = (list: Projet[]) => {
          const idx = list.findIndex(p => p.id === projet.id);
          if (idx !== -1) {
            list[idx] = Object.assign(new Projet(), list[idx], { fichiers: updatedFichiers });
          }
        };

        applyUpdate(this.projets);
        applyUpdate(this.filteredProjets);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.snackBar.open("Erreur lors de l'upload", 'Fermer', { duration: 3000 });
      },
    });
  }

  onDeleteFile(projet: Projet, fichier: FichierInfo): void {
    this.fichierService.deleteFile('PROJET', projet.id, fichier.id).subscribe({
      next: () => {
        const applyRemove = (list: Projet[]) => {
          const idx = list.findIndex(p => p.id === projet.id);
          if (idx !== -1) {
            list[idx] = Object.assign(new Projet(), list[idx], {
              fichiers: list[idx].fichiers?.filter(f => f.id !== fichier.id) ?? [],
            });
          }
        };

        applyRemove(this.projets);
        applyRemove(this.filteredProjets);
        this.cdr.markForCheck();
        this.snackBar.open('Fichier supprimé', 'Fermer', { duration: 2000 });
      },
      error: (err) => {
        console.error('Erreur suppression fichier:', err);
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      },
    });
  }

  // ─── Dialogs ─────────────────────────────────────────────────────────────────

  openCreateDialog(): void {
    this.dialog.open(ProjetForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' },
    }).afterClosed().subscribe(result => {
      if (result) {
        this.loadProjets();
        this.snackBar.open('Projet créé avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openEditDialog(projet: Projet): void {
    this.dialog.open(ProjetForm, {
      width: '600px',
      data: { mode: 'edit', projet },
    }).afterClosed().subscribe(result => {
      if (result) {
        this.loadProjets();
        this.snackBar.open('Projet mis à jour avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }

  openDeleteDialog(projet: Projet): void {
    this.dialog.open(ConfirmationDialog, {
      width: '400px',
      data: {
        title: 'Supprimer le projet',
        message: `Êtes-vous sûr de vouloir supprimer le projet "${projet.designation}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler',
      },
    }).afterClosed().subscribe(result => {
      if (result) this.deleteProjet(projet.id);
    });
  }

  deleteProjet(id: number): void {
    this.projetService.deleteProjet(id).subscribe({
      next: () => {
        // Revenir à la page précédente si on supprime le dernier élément
        if (this.projets.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.loadProjets();
        this.snackBar.open('Projet supprimé avec succès', 'Fermer', { duration: 3000 });
      },
      error: (error) => {
        console.error('Erreur suppression:', error);
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      },
    });
  }

  // ─── Email ───────────────────────────────────────────────────────────────────

  onEditEmail(email: EmailDto): void {
    this.dialog.open(EditEmailDialog, {
      width: '400px',
      data: { email, emailType: 'EMAIL_PROJET' } as EditEmailDialogData,
    }).afterClosed().subscribe(result => {
      if (result) this.loadProjets();
    });
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      EN_ATTENTE: 'bg-yellow-100 text-yellow-800',
      EN_COURS:   'bg-blue-100 text-blue-800',
      TERMINE:    'bg-green-100 text-green-800',
      ANNULE:     'bg-red-100 text-red-800',
    };
    return colors[status] ?? 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      EN_ATTENTE: 'En attente',
      EN_COURS:   'En cours',
      TERMINE:    'Terminé',
      ANNULE:     'Annulé',
    };
    return labels[status] ?? status;
  }

  getSimpleEmails(projet: Projet): string {
    return projet.emails?.map(e => e.email).join(', ') ?? '';
  }
}