// src/app/gestion_de_taches/pages/activite-details/activite-details.ts
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditableActivieDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { Activite } from '../../models/activite.model';
import { ActivatedRoute, Router } from '@angular/router';
import { ActiviteService } from '../../services/activite.service';
import { TacheService } from '../../services/tache.service';
import { AuthService } from '../../services/AuthService';
import { FichierService } from '../../services/fichier-service';
import { NotificationService } from '../../services/notification.service';
import { OnlyOfficeViewer } from '../../components/cards/onlyoffice-viewer/onlyoffice-viewer';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { MaterialModule } from '../../material.module';
import { SafeResourceUrl } from '@angular/platform-browser';
import { Observable, switchMap } from 'rxjs';
import { TacheSection } from '../../components/tache-section/tache-section';
import { User } from '../../models/user';
import { Tache } from '../../models/tache.model';
import { ActiviteDetail } from '../../components/activite-detail/activite-detail';

@Component({
  selector: 'app-activite-details',
  imports: [CommonModule, OnlyOfficeViewer, PdfModal, MaterialModule, ActiviteDetail, TacheSection],
  templateUrl: './activite-details.html',
  styleUrl: './activite-details.css',
})
export class ActiviteDetails implements OnInit {

  activiteId!: number;
  activite?: Activite;
  tacheSelected?: Tache;
  loading = true;
  showScrollTop = false;

  // Visualisation fichiers
  isPdfModalOpen = false;
  pdfPreviewUrl: SafeResourceUrl | string = '';
  fileName = '';
  isOnlyOfficeModalOpen = false;
  fichierInfo: FichierInfo | null = null;

  private user = signal<User | null>(null);

  getUser(): User | null {
    return this.user();
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private activiteService: ActiviteService,
    private tacheService: TacheService,
    private authService: AuthService,
    private fichierService: FichierService,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    // Lire l'id de la route en premier, indépendamment de l'auth
    this.activiteId = +this.route.snapshot.paramMap.get('id')!;

    // S'abonner à l'utilisateur courant (sans déclencher loadActivite à chaque émission)
    this.authService.user$.subscribe((user) => {
      this.user.set(user);
    });

    this.loadActivite();
  }

  // ─── Navigation ──────────────────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/app/activites']);
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  scrollTo(elementId: string): void {
    document.getElementById(elementId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const shouldShow = window.scrollY > 300;
    if (shouldShow !== this.showScrollTop) {
      this.showScrollTop = shouldShow;
      this.cdr.markForCheck();
    }
  }

  // ─── Chargement ──────────────────────────────────────────────────────────────

  loadActivite(): void {
    this.loading = true;
    this.activiteService.getById(this.activiteId).subscribe({
      next: (data) => {
        this.activite = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur chargement activite', err);
        this.notification.error('Impossible de charger l\'activité');
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  // ─── Sélection tâche ─────────────────────────────────────────────────────────

  onTacheSelected(tache: Tache): void {
    this.tacheSelected = tache;
  }

  onTacheSelectedDelete(tache: Tache): void {
    if (this.tacheSelected?.id === tache.id) {
      this.tacheSelected = undefined;
    }
  }

  // ─── Édition inline ──────────────────────────────────────────────────────────

  onApplieELementChange(editableChamps: EditableActivieDto): void {
    type CaseType = 'DESIGNATION' | 'DESCRIPTION' | 'DATE_DEBUT' | 'DATE_FIN';

    const config: Record<CaseType, {
      observable: Observable<Activite>;
      success: string;
      error: string;
    }> = {
      DESIGNATION: {
        observable: this.activiteService.updateActiviteDesignation(
          editableChamps.activite.id, { texte: editableChamps.texte }
        ),
        success: 'Désignation mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la désignation.',
      },
      DESCRIPTION: {
        observable: this.activiteService.updateActiviteDescription(
          editableChamps.activite.id, { texte: editableChamps.texte }
        ),
        success: 'Description mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la description.',
      },
      DATE_DEBUT: {
        observable: this.activiteService.updateActiviteDateDebut(
          editableChamps.activite.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de début mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de début.',
      },
      DATE_FIN: {
        observable: this.activiteService.updateActiviteDateFin(
          editableChamps.activite.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de fin mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de fin.',
      },
    };

    const action = config[editableChamps.type as CaseType];
    if (!action) return;

    action.observable.subscribe({
      next: (updated) => {
        this.activite = updated;
        this.notification.success(action.success);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error(err);
        this.notification.error(action.error);
      },
    });
  }

  // ─── Fichiers ────────────────────────────────────────────────────────────────

  onViewFile(file: FichierInfo): void {
    const extension = file.nomFichier.split('.').pop()?.toLowerCase();
    this.fileName = file.nomFichier;

    if (extension === 'pdf') {
      this.pdfPreviewUrl = file.url;
      this.isPdfModalOpen = true;
    } else {
      this.fichierInfo = file;
      this.isOnlyOfficeModalOpen = true;
    }
  }

  onPdfModalClosed(): void {
    this.isPdfModalOpen = false;
    this.pdfPreviewUrl = '';
  }

  onOnlyOfficeModalClosed(): void {
    this.isOnlyOfficeModalOpen = false;
    this.fichierInfo = null;
  }

  onViewerError(error: string): void {
    console.error('Erreur du viewer ONLYOFFICE:', error);
    this.notification.error('Impossible d\'ouvrir le fichier');
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
          callbackurl: f.callbackurl ?? '',
        }));
        this.activite = { ...this.activite!, fichiers: updatedFichiers };
        this.notification.success('Fichier(s) ajouté(s) avec succès');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.notification.error("Erreur lors de l'upload");
      },
    });
  }

  onDeleteFile(file: FichierInfo, parentType: 'activite' | 'tache', parentId: number): void {
    if (!confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) return;

    const deleteObservable: Observable<any> = parentType === 'activite'
      ? this.activiteService.deleteFichier(parentId, file.id)
      : this.tacheService.deleteFichier(parentId, file.id);

    deleteObservable.subscribe({
      next: () => {
        if (parentType === 'activite' && this.activite) {
          // Correction : mettre à jour this.activite (et non activiteSelected)
          this.activite = {
            ...this.activite,
            fichiers: (this.activite.fichiers ?? []).filter(f => f.id !== file.id),
          };
        } else if (parentType === 'tache' && this.tacheSelected) {
          this.tacheSelected = {
            ...this.tacheSelected,
            fichiers: (this.tacheSelected.fichiers ?? []).filter(f => f.id !== file.id),
          };
        }
        this.notification.success('Fichier supprimé avec succès');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur suppression fichier', err);
        this.notification.error('Erreur lors de la suppression');
      },
    });
  }
}