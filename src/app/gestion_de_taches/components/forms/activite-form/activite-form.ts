// src/app/gestion_de_taches/components/activite-form/activite-form.component.ts
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';


import { DatePipe } from '@angular/common';
import { MaterialModule } from '../../../material.module';
import { User } from '../../../models/user';
import { ActiviteService } from '../../../services/activite.service';
import { AuthService } from '../../../services/AuthService';
import { Activite } from '../../../models/activite.model';
import { EmailDto } from '../../../interfaces/base-entity-gestion';



@Component({
  selector: 'app-activite-form',
  templateUrl: './activite-form.html',
  imports:[ReactiveFormsModule,MaterialModule,DatePipe],
  styleUrls: ['./activite-form.css']
})
export class ActiviteForm implements OnInit {
  activiteForm: FormGroup;
  isEditMode: boolean;
  user:User|null=null;

  fichiers: File[] = [];
  fichiersExistants: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private activiteService: ActiviteService,
    private authService: AuthService,
    public dialogRef: MatDialogRef<ActiviteForm>,
    @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit';projetId?:number; activite?: Activite }
  ) {
    this.isEditMode = data.mode === 'edit';
    this.activiteForm = this.fb.group({
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
    if (this.isEditMode && this.data.activite) {
      this.activiteForm.patchValue({
        designation: this.data.activite.designation,
        description: this.data.activite.description,
        dateDebut: this.formatDateForInput(this.data.activite.dateDebut),
        dateFin: this.formatDateForInput(this.data.activite.dateFin),
        emails: this.data.activite.emails
        ?.map((e: EmailDto) => e.email)
        .join(', '),
        status: this.data.activite.status
      });
      this.fichiersExistants = this.data.activite.fichiers || [];
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
    if (this.activiteForm.invalid) return;

    this.loading = true;
    const formValue = this.activiteForm.value;

    if (this.isEditMode && this.data.activite) {
     const updateDto = {
  designation: formValue.designation,
  description: formValue.description,
  dateDebut: formValue.dateDebut,
  dateFin: formValue.dateFin,
  status: formValue.status,
  emails:formValue.emails.trim().split(","),
  createurId: this.user?.id||0
};
      this.activiteService.update(this.data.activite.id,updateDto).subscribe({
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
      
      this.activiteService.create(this.data.projetId||0,createDto, this.fichiers).subscribe({
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