// src/app/gestion_de_taches/pages/tache-details/tache-details.ts
import {
  ChangeDetectorRef,
  Component,
  HostListener,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditableTacheDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { Tache } from '../../models/tache.model';
import { ActivatedRoute, Router } from '@angular/router';
import { TacheService } from '../../services/tache.service';
import { AuthService } from '../../services/AuthService';
import { FichierService } from '../../services/fichier-service';
import { NotificationService } from '../../services/notification.service';
import { OnlyOfficeViewer } from '../../components/cards/onlyoffice-viewer/onlyoffice-viewer';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { MaterialModule } from '../../material.module';
import { SafeResourceUrl } from '@angular/platform-browser';
import { Observable, switchMap } from 'rxjs';
import { User } from '../../models/user';
import { TacheDetail } from '../../components/tache-detail/tache-detail';

@Component({
  selector: 'app-tache-details',
  imports: [CommonModule, OnlyOfficeViewer, PdfModal, MaterialModule, TacheDetail],
  templateUrl: './tache-details.html',
  styleUrl: './tache-details.css',
})
export class TacheDetails implements OnInit {

  tacheId!: number;
  tache?: Tache;
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
    private tacheService: TacheService,
    private authService: AuthService,
    private fichierService: FichierService,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.tacheId = +this.route.snapshot.paramMap.get('id')!;
    this.authService.user$.subscribe((user) => this.user.set(user));
    this.loadTache();
  }

  // ─── Navigation ──────────────────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/app/taches']);
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

  loadTache(): void {
    this.loading = true;
    this.tacheService.getById(this.tacheId).subscribe({
      next: (data) => {
        this.tache = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur chargement tâche', err);
        this.notification.error('Impossible de charger la tâche');
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  // ─── Édition inline ──────────────────────────────────────────────────────────

  onApplieELementChange(editableChamps: EditableTacheDto): void {
    type CaseType = 'DESIGNATION' | 'DESCRIPTION' | 'DATE_DEBUT' | 'DATE_FIN';

    const config: Record<CaseType, {
      observable: Observable<Tache>;
      success: string;
      error: string;
    }> = {
      DESIGNATION: {
        observable: this.tacheService.updateTacheDesignation(
          editableChamps.tache.id, { texte: editableChamps.texte }
        ),
        success: 'Désignation mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la désignation.',
      },
      DESCRIPTION: {
        observable: this.tacheService.updateTacheDescription(
          editableChamps.tache.id, { texte: editableChamps.texte }
        ),
        success: 'Description mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la description.',
      },
      DATE_DEBUT: {
        observable: this.tacheService.updateTacheDateDebut(
          editableChamps.tache.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de début mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de début.',
      },
      DATE_FIN: {
        observable: this.tacheService.updateTacheDateFin(
          editableChamps.tache.id, { date: editableChamps.date ?? new Date() }
        ),
        success: 'Date de fin mise à jour avec succès !',
        error: 'Erreur lors de la mise à jour de la date de fin.',
      },
    };

    const action = config[editableChamps.type as CaseType];
    if (!action) return;

    action.observable.subscribe({
      next: (updated) => {
        this.tache = updated;
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
    this.notification.error("Impossible d'ouvrir le fichier");
  }

  onAddFiles(tache: Tache, files: File[]): void {
    this.fichierService.uploadFiles('TACHE', tache.id, files).pipe(
      switchMap(() => this.fichierService.listFiles('TACHE', tache.id))
    ).subscribe({
      next: (fichiers) => {
        const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
          id: f.id,
          nomFichier: f.nomFichier ?? '',
          url: f.url ?? '',
          type: f.type ?? '',
          callbackurl: f.callbackurl ?? '',
        }));
        this.tache = { ...this.tache!, fichiers: updatedFichiers };
        this.notification.success('Fichier(s) ajouté(s) avec succès');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.notification.error("Erreur lors de l'upload");
      },
    });
  }

  // Correction : parentType distingue 'tache' (la tâche principale) et 'sous-tache' si besoin futur
  // Pour l'instant une seule cible : this.tache
  onDeleteFile(file: FichierInfo, tacheId: number): void {
    if (!confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) return;

    this.tacheService.deleteFichier(tacheId, file.id).subscribe({
      next: () => {
        if (this.tache) {
          this.tache = {
            ...this.tache,
            fichiers: (this.tache.fichiers ?? []).filter(f => f.id !== file.id),
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