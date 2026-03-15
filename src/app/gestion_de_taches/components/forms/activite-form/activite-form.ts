import { Component, Inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { filter, finalize, Subject, takeUntil } from 'rxjs';

import { MaterialModule } from '../../../material.module';
import { User } from '../../../models/user';
import { ActiviteService } from '../../../services/activite.service';
import { AuthService } from '../../../services/AuthService';
import { Activite } from '../../../models/activite.model';
import { EmailDto, Projet } from '../../../interfaces/base-entity-gestion';
import { ProjetService } from '../../../services/projet-service';
import { ProjetDto } from '../../../interfaces/generals';
import { ProjetForm } from '../../projet-form/projet-form';
import { ConfirmationDialog } from '../../confirmation-dialog/confirmation-dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-activite-form',
  templateUrl: './activite-form.html',
  imports: [ReactiveFormsModule, MaterialModule, CommonModule],
  styleUrls: ['./activite-form.css']
})
export class ActiviteForm implements OnInit, OnDestroy {

  activiteForm!: FormGroup;
  isEditMode: boolean;
  user: User | null = null;
  projetDtos: ProjetDto[] = [];
  fichiers: File[] = [];
  fichiersExistants: any[] = [];
  loading = false;

  private destroy$ = new Subject<void>();
  loadingProjets = false;



  constructor(
    private fb: FormBuilder,
    private activiteService: ActiviteService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private projetService: ProjetService,
    public dialogRef: MatDialogRef<ActiviteForm>,
    @Inject(MAT_DIALOG_DATA) public data: {
      mode: 'create' | 'edit';
      projetId?: number;
      activite?: Activite;
    }
  ) {
    this.isEditMode = data.mode === 'edit';
      this.initForm();
  }

 ngOnInit(): void {


  // Démarrer le spinner immédiatement si on sait qu'on va charger
  if (!this.isEditMode && !this.data.projetId) {
    this.loadingProjets = true; // ← ici, avant tout appel async
  }

  this.authService.user$
    .pipe(
      takeUntil(this.destroy$),
      filter(user => !!user) 
    )
    .subscribe(user => {
      this.user = user;

      if (!this.isEditMode && !this.data.projetId) {
        this.loadProjets(user!.email);
        this.cdr.detectChanges();
       
      }
    });

  if (this.isEditMode && this.data.activite) {
    this.patchForm(this.data.activite);
  }
}

private loadProjets(email: string): void {
  // Ne pas remettre loadingProjets = true ici, déjà fait dans ngOnInit
  this.projetService.getListProjetDto(email)
    .pipe(
      takeUntil(this.destroy$),
      finalize(() => this.loadingProjets = false) // ← s'exécute toujours, succès ou erreur
    )
    .subscribe({
      next: projets => {
        this.projetDtos = projets;
        this.loadingProjets=false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement projets:', err);
      }
    });
}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  // ─── Dialogs ─────────────────────────────────────────────────────────────────

  openCreateDialog(): void {
    this.dialog.open(ProjetForm, {
      width: '600px',
      maxHeight: '100vh',
      data: { mode: 'create' },
    }).afterClosed().subscribe(result => {
      if (result) {
        // Recharger la liste et sélectionner automatiquement le nouveau projet
        this.loadingProjets = true;
        this.cdr.detectChanges();

        this.projetService.getListProjetDto(this.user?.email || '')
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.loadingProjets = false;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: projets => {
              this.projetDtos = projets;

              // Si le résultat contient l'id du projet créé, on le sélectionne
              // Sinon on prend le dernier de la liste (le plus récent)
              if (projets.length > 0) {
                const nouveauProjet = result?.id
                  ? projets.find(p => p.id === result.id) ?? projets[projets.length - 1]
                  : projets[projets.length - 1];
                this.activiteForm.get('projetId')?.setValue(nouveauProjet.id);
              }

              this.snackBar.open('Projet créé avec succès', 'Fermer', { duration: 3000 });
            },
            error: (err) => {
              console.error('Erreur rechargement projets:', err);
              this.snackBar.open('Erreur lors du chargement des projets', 'Fermer', { duration: 3000 });
            }
          });
      }
    });
  }

  openEditDialog(projet: Projet): void {
    this.dialog.open(ProjetForm, {
      width: '600px',
      data: { mode: 'edit', projet },
    }).afterClosed().subscribe(result => {
      if (result) {
        this.loadProjets(this.user?.email||'');
        this.snackBar.open('Projet mis à jour avec succès', 'Fermer', { duration: 3000 });
      }
    });
  }


  // ─── Initialisation du formulaire ───────────────────────────────────────────

  private initForm(): void {
    this.activiteForm = this.fb.group({
      designation: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      emails: [''],
      projetId: [null],
      dateDebut: [this.getTodayString(), Validators.required],
      dateFin: ['', Validators.required],
      status: ['EN_ATTENTE', Validators.required]
    });

    // Validation conditionnelle : projetId requis si pas de projetId en data
    if (!this.isEditMode && !this.data.projetId) {
      this.activiteForm.get('projetId')?.setValidators(Validators.required);
      this.activiteForm.get('projetId')?.updateValueAndValidity();
    }
  }

  private patchForm(activite: Activite): void {
    this.activiteForm.patchValue({
      designation: activite.designation,
      description: activite.description,
      dateDebut: this.formatDateForInput(activite.dateDebut),
      dateFin: this.formatDateForInput(activite.dateFin),
      emails: activite.emails?.map((e: EmailDto) => e.email).join(', ') ?? '',
      status: activite.status
    });
    this.fichiersExistants = activite.fichiers || [];
  }

  // ─── Chargement des projets ──────────────────────────────────────────────────




  // ─── Gestion des fichiers ────────────────────────────────────────────────────

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.fichiers = [...this.fichiers, ...Array.from(input.files)];
    }
  }

  removeFichier(index: number): void {
    this.fichiers.splice(index, 1);
  }

  removeFichierExistant(index: number): void {
    this.fichiersExistants.splice(index, 1);
  }

  // ─── Soumission ──────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.activiteForm.invalid) return;

    this.loading = true;
    const formValue = this.activiteForm.value;
    const emails = this.parseEmails(formValue.emails);

    if (this.isEditMode && this.data.activite) {
      const updateDto = {
        designation: formValue.designation,
        description: formValue.description,
        dateDebut: formValue.dateDebut,
        dateFin: formValue.dateFin,
        status: formValue.status,
        emails,
        createurId: this.user?.id ?? 0
      };

      this.activiteService.update(this.data.activite.id, updateDto)
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

      const projetId = this.data.projetId ?? formValue.projetId ?? 0;

      this.activiteService.create(projetId, createDto, this.fichiers)
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