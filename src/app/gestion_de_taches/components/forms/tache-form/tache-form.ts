// src/app/gestion_de_taches/components/tache-form/tache-form.component.ts
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';


import { DatePipe } from '@angular/common';
import { MaterialModule } from '../../../material.module';
import { User } from '../../../models/user';
import { TacheService } from '../../../services/tache.service';
import { AuthService } from '../../../services/AuthService';
import { Tache } from '../../../models/tache.model';
import { EmailDto } from '../../../interfaces/base-entity-gestion';



@Component({
  selector: 'app-tache-form',
  templateUrl: './tache-form.html',
  imports:[ReactiveFormsModule,MaterialModule,DatePipe],
  styleUrls: ['./tache-form.css']
})
export class TacheForm implements OnInit {
  tacheForm: FormGroup;
  isEditMode: boolean;
  user:User|null=null;

  fichiers: File[] = [];
  fichiersExistants: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private tacheService: TacheService,
    private authService: AuthService,
    public dialogRef: MatDialogRef<TacheForm>,
    @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit';projetId?:number; tache?: Tache }
  ) {
    this.isEditMode = data.mode === 'edit';
    this.tacheForm = this.fb.group({
      designation: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      emails:[''],
      dateDebut: [new Date(), Validators.required],
      dateFin: ['', Validators.required],
      status: ['EN_ATTENTE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.authService.user$.subscribe((user)=>{
      this.user=user;
    })
    if (this.isEditMode && this.data.tache) {
      this.tacheForm.patchValue({
        designation: this.data.tache.designation,
        description: this.data.tache.description,
        dateDebut: this.formatDateForInput(this.data.tache.dateDebut),
        dateFin: this.formatDateForInput(this.data.tache.dateFin),
        emails: this.data.tache.emails
        ?.map((e: EmailDto) => e.email)
        .join(', '),
        status: this.data.tache.status
      });
      this.fichiersExistants = this.data.tache.fichiers || [];
    }
  }
private formatDateForInput(date: Date | string | undefined): string {
  if (!date) return '';
  const d = new Date(date);
  // Retourne YYYY-MM-DD
  return d.toISOString().split('T')[0];
}
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

  onSubmit(): void {
    if (this.tacheForm.invalid) return;

    this.loading = true;
    const formValue = this.tacheForm.value;

    if (this.isEditMode && this.data.tache) {
     const updateDto = {
  designation: formValue.designation,
  description: formValue.description,
  dateDebut: formValue.dateDebut,
  dateFin: formValue.dateFin,
  status: formValue.status,
  emails:formValue.emails.trim().split(","),
  createurId: this.user?.id||0
};
      this.tacheService.update(this.data.tache.id,updateDto).subscribe({
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
          emails:formValue.emails.trim().split(","),
          createurId: this.user?.id||0
      };
      
      this.tacheService.create(this.data.projetId||0,createDto, this.fichiers).subscribe({
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

  getDateMin(): string {
    const today = new Date();
    return today.toISOString().split('T')[0];
  }
}