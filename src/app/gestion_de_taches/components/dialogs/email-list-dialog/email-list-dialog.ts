import { Component, Inject, Output, EventEmitter, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EmailDto, MailTYPE } from '../../../interfaces/base-entity-gestion';
import { EmailService } from '../../../services/emails/email-service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmailCard } from '../../cards/email-card/email-card';

@Component({
  selector: 'app-email-list-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    EmailCard,
  ],
  templateUrl: './email-list-dialog.html',
})
export class EmailListDialog {
  @Output() emailAdded = new EventEmitter<EmailDto>();
  @Output() emailUpdated = new EventEmitter<EmailDto>();
  @Output() emailDeleted = new EventEmitter<number>();

  @ViewChild('newEmailInput') newEmailInput?: ElementRef<HTMLInputElement>;

  showAddInput = false;
  newEmail = '';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { emails: EmailDto[]; emailType: MailTYPE; entiteId: number },
    private dialogRef: MatDialogRef<EmailListDialog>,
    private emailService: EmailService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  addEmail(): void {
    const email = this.newEmail.trim();
    if (!email) return;

    this.emailService.addEmail(this.data.emailType, this.data.entiteId, email).subscribe({
      next: (newEmail) => {
        this.data.emails = [...this.data.emails, newEmail];
        this.emailAdded.emit(newEmail);
        this.snackBar.open('Email ajouté', 'Fermer', { duration: 2000 });
        this.newEmail = '';
        this.showAddInput = false;
        this.cdr.detectChanges(); // force la mise à jour de la liste
      },
      error: (err) => {
        console.error('Add email failed', err);
        this.snackBar.open('Erreur lors de l\'ajout', 'Fermer', { duration: 3000 });
      }
    });
  }

  cancelAdd(): void {
    this.showAddInput = false;
    this.newEmail = '';
  }

  onEmailUpdated(updatedEmail: EmailDto): void {
    const index = this.data.emails.findIndex(e => e.id === updatedEmail.id);
    if (index !== -1) {
      this.data.emails[index] = updatedEmail;
    }
    this.emailUpdated.emit(updatedEmail);
  }

  onEmailDeleted(id: number): void {
    this.data.emails = this.data.emails.filter(e => e.id !== id);
    this.emailDeleted.emit(id);
  }
}