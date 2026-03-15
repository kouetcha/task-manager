// src/app/gestion_de_taches/components/projet-form/projet-form.component.ts
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProjetService } from '../../services/projet-service';
import { AuthService } from '../../services/AuthService';
import { EmailDto, Projet } from '../../interfaces/base-entity-gestion';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MaterialModule } from '../../material.module';
import { DatePipe } from '@angular/common';
import { User } from '../../models/user';


@Component({
  selector: 'app-projet-form',
  templateUrl: './projet-form.html',
  imports:[ReactiveFormsModule,MaterialModule,DatePipe],
  styleUrls: ['./projet-form.css']
})
export class ProjetForm implements OnInit {
  projetForm: FormGroup;
  isEditMode: boolean;
  user:User|null=null;

  fichiers: File[] = [];
  fichiersExistants: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private projetService: ProjetService,
    private authService: AuthService,
    public dialogRef: MatDialogRef<ProjetForm>,
    @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit'; projet?: Projet }
  ) {
    this.isEditMode = data.mode === 'edit';
    this.projetForm = this.fb.group({
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
    if (this.isEditMode && this.data.projet) {
      this.projetForm.patchValue({
        designation: this.data.projet.designation,
        description: this.data.projet.description,
        dateDebut: this.formatDateForInput(this.data.projet.dateDebut),
        dateFin: this.formatDateForInput(this.data.projet.dateFin),
        emails: this.data.projet.emails
        ?.map((e: EmailDto) => e.email)
        .join(', '),
        status: this.data.projet.status
      });
      this.fichiersExistants = this.data.projet.fichiers || [];
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
    if (this.projetForm.invalid) return;

    this.loading = true;
    const formValue = this.projetForm.value;

    if (this.isEditMode && this.data.projet) {
     const updateDto = {
  designation: formValue.designation,
  description: formValue.description,
  dateDebut: formValue.dateDebut,
  dateFin: formValue.dateFin,
  status: formValue.status,
  emails:formValue.emails.trim().split(","),
  createurId: this.user?.id||0
};
      this.projetService.updateProjet(this.data.projet.id,updateDto).subscribe({
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
      
      this.projetService.createProjet(createDto, this.fichiers).subscribe({
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