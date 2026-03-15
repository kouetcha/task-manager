import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EmailDto, MailTYPE } from '../../interfaces/base-entity-gestion';
import { EmailService } from '../../services/emails/email-service';

export interface EditEmailDialogData {
  email: EmailDto;
  emailType: MailTYPE;
}

@Component({
  selector: 'app-edit-email-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title>Modifier l'email</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="w-full">
      
        <input matInput [(ngModel)]="newEmail" type="email" required>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="!newEmail || newEmail === data.email.email">
        Enregistrer
      </button>
    </mat-dialog-actions>
  `,
styles: [`
  mat-form-field { width: 100%; }
  /* Annule toute décoration de texte sur l'input du dialogue */
  .edit-email-input {
    text-decoration: none !important;
  }
`]
})
export class EditEmailDialog {
  newEmail: string;

  constructor(
    public dialogRef: MatDialogRef<EditEmailDialog>,
    @Inject(MAT_DIALOG_DATA) public data: EditEmailDialogData,
    private emailService: EmailService,
    private snackBar: MatSnackBar
  ) {
    this.newEmail = data.email.email;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (!this.newEmail || this.newEmail === this.data.email.email) {
      return;
    }

   
          // modifier le nouvel email
          this.emailService.updateEmail(this.data.emailType, this.data.email.entiteId, this.data.email.id,this.newEmail)
            .subscribe({
              next: (newEmailDto) => {
                this.snackBar.open('Email modifié avec succès', 'Fermer', { duration: 3000 });
                this.dialogRef.close(newEmailDto); // Retourne le nouvel email
              },
              error: (err) => {
                console.error('Erreur lors de l\'ajout du nouvel email', err);
                this.snackBar.open('Erreur lors de la modification', 'Fermer', { duration: 3000 });
                this.dialogRef.close(); // Ferme sans retour
              }
            });
        }
}