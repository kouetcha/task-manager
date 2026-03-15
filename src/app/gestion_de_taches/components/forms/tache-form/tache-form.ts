// src/app/gestion_de_taches/components/tache-form/tache-form.component.ts
import { Component, Inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { filter, finalize, Subject, takeUntil } from 'rxjs';

import { MaterialModule } from '../../../material.module';
import { User } from '../../../models/user';
import { TacheService } from '../../../services/tache.service';
import { AuthService } from '../../../services/AuthService';
import { Tache } from '../../../models/tache.model';
import { EmailDto } from '../../../interfaces/base-entity-gestion';
import { ActiviteService } from '../../../services/activite.service';

import { MatSnackBar } from '@angular/material/snack-bar';
import { ActiviteForm } from '../activite-form/activite-form';

// Adaptez ce type selon votre interface réelle (ex: depuis generals.ts)
export interface ActiviteDto {
  id: number;
  designation: string;
}

@Component({
  selector: 'app-tache-form',
  templateUrl: './tache-form.html',
  imports: [ReactiveFormsModule, MaterialModule, CommonModule],
  styleUrls: ['./tache-form.css']
})
export class TacheForm implements OnInit, OnDestroy {

  tacheForm: FormGroup;
  isEditMode: boolean;
  user: User | null = null;

  activiteDtos: ActiviteDto[] = [];
  fichiers: File[] = [];
  fichiersExistants: any[] = [];
  loading = false;
  loadingActivites = false;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private tacheService: TacheService,
    private authService: AuthService,
    private activiteService: ActiviteService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    public dialogRef: MatDialogRef<TacheForm>,
    @Inject(MAT_DIALOG_DATA) public data: {
      mode: 'create' | 'edit';
      activiteId?: number; // Si fourni, on saute le select
      tache?: Tache;
    }
  ) {
    this.isEditMode = data.mode === 'edit';

    this.tacheForm = this.fb.group({
      designation: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      emails: [''],
      activiteId: [null],
      dateDebut: [this.getTodayString(), Validators.required],
      dateFin: ['', Validators.required],
      status: ['EN_ATTENTE', Validators.required]
    });

    // activiteId requis uniquement si pas fourni en data
    if (!this.isEditMode && !this.data.activiteId) {
      this.tacheForm.get('activiteId')?.setValidators(Validators.required);
      this.tacheForm.get('activiteId')?.updateValueAndValidity();
    }
  }

  ngOnInit(): void {
    if (!this.isEditMode && !this.data.activiteId) {
      this.loadingActivites = true;
    }

    this.authService.user$
      .pipe(
        takeUntil(this.destroy$),
        filter(user => !!user)
      )
      .subscribe(user => {
        this.user = user;

        if (!this.isEditMode && !this.data.activiteId) {
          this.loadActivites(user!.email);
          this.cdr.detectChanges();
        }
      });

    if (this.isEditMode && this.data.tache) {
      this.patchForm(this.data.tache);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── Chargement des activités ────────────────────────────────────────────────

  private loadActivites(email: string): void {
    this.activiteService.getListActiviteDto(email)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingActivites = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: activites => {
          this.activiteDtos = activites;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Erreur chargement activités:', err);
        }
      });
  }

  // ─── Ouvrir ActiviteForm ──────────────────────────────────────────────────────
  // ActiviteForm gère déjà la cascade : si pas de projet → propose d'en créer un

  openCreateActiviteDialog(): void {
    this.dialog.open(ActiviteForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' }, // Pas de projetId → ActiviteForm affiche le select projet
    }).afterClosed().subscribe(result => {
      if (result) {
        this.loadingActivites = true;
        this.cdr.detectChanges();

        this.activiteService.getListActiviteDto(this.user?.email || '')
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.loadingActivites = false;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: activites => {
              this.activiteDtos = activites;

              // Sélectionner automatiquement la nouvelle activité
              if (activites.length > 0) {
                const nouvelleActivite = result?.id
                  ? (activites.find(a => a.id === result.id) ?? activites[activites.length - 1])
                  : activites[activites.length - 1];
                this.tacheForm.get('activiteId')?.setValue(nouvelleActivite.id);
              }

              this.snackBar.open('Activité créée avec succès', 'Fermer', { duration: 3000 });
            },
            error: (err) => {
              console.error('Erreur rechargement activités:', err);
              this.snackBar.open('Erreur lors du chargement des activités', 'Fermer', { duration: 3000 });
            }
          });
      }
    });
  }

  // ─── Patch formulaire (mode édition) ────────────────────────────────────────

  private patchForm(tache: Tache): void {
    this.tacheForm.patchValue({
      designation: tache.designation,
      description: tache.description,
      dateDebut: this.formatDateForInput(tache.dateDebut),
      dateFin: this.formatDateForInput(tache.dateFin),
      emails: tache.emails?.map((e: EmailDto) => e.email).join(', ') ?? '',
      status: tache.status
    });
    this.fichiersExistants = tache.fichiers || [];
  }

  // ─── Gestion des fichiers ────────────────────────────────────────────────────

  onFileSelected(event: any): void {
    const files = Array.from(event.target.files) as File[];
    this.fichiers = [...this.fichiers, ...files];
  }

  removeFichier(index: number): void {
    this.fichiers.splice(index, 1);
  }

  removeFichierExistant(index: number): void {
    this.fichiersExistants.splice(index, 1);
  }

  // ─── Soumission ──────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.tacheForm.invalid) return;

    this.loading = true;
    const formValue = this.tacheForm.value;
    const emails = this.parseEmails(formValue.emails);

    if (this.isEditMode && this.data.tache) {
      const updateDto = {
        designation: formValue.designation,
        description: formValue.description,
        dateDebut: formValue.dateDebut,
        dateFin: formValue.dateFin,
        status: formValue.status,
        emails,
        createurId: this.user?.id ?? 0
      };

      this.tacheService.update(this.data.tache.id, updateDto)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => this.dialogRef.close(true),
          error: (error) => {
            console.error('Erreur mise à jour:', error);
            this.loading = false;
          }
        });

    } else {
      const createDto = {
        designation: formValue.designation,
        description: formValue.description,
        dateDebut: formValue.dateDebut,
        dateFin: formValue.dateFin,
        status: formValue.status,
        emails,
        createurId: this.user?.id ?? 0
      };

      // Priorité : activiteId fourni en data, sinon celui du formulaire
      const activiteId = this.data.activiteId ?? formValue.activiteId ?? 0;

      this.tacheService.create(activiteId, createDto, this.fichiers)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => this.dialogRef.close(true),
          error: (error) => {
            console.error('Erreur création:', error);
            this.loading = false;
          }
        });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  // ─── Utilitaires ─────────────────────────────────────────────────────────────

  private parseEmails(raw: string): string[] {
    return raw?.trim()
      ? raw.trim().split(',').map(e => e.trim()).filter(e => e.length > 0)
      : [];
  }

  private formatDateForInput(date: Date | string | undefined): string {
    if (!date) return '';
    return new Date(date).toISOString().split('T')[0];
  }

  getTodayString(): string {
    return new Date().toISOString().split('T')[0];
  }

  getDateMin(): string {
    return this.getTodayString();
  }
}