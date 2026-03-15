import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CreateurDto, EmailDto, MailTYPE } from '../../../interfaces/base-entity-gestion';
import { EmailService } from '../../../services/emails/email-service';

import { MaterialModule } from '../../../material.module';
import { EmailListDialog } from '../../dialogs/email-list-dialog/email-list-dialog';
import { EmailCard } from '../../cards/email-card/email-card';
import { User } from '../../../models/user';

@Component({
  selector: 'app-email-list',
  standalone: true,
  imports: [CommonModule, FormsModule, MaterialModule, EmailCard],
  templateUrl: './email-list.html',
})
export class EmailList implements AfterViewInit {
  @Input() emails: EmailDto[] = [];
  @Input() isCreateur!: boolean;
  @Input() emailType!: MailTYPE;
  @Input() entiteId!: number;
  @Input() pageSize: number = 3;
  @Input() user!: User;
  @Input() createur!: CreateurDto;

  @Output() emailAdded = new EventEmitter<EmailDto>();
  @Output() emailUpdated = new EventEmitter<EmailDto>();
  @Output() emailDeleted = new EventEmitter<number>();

  @ViewChild('newEmailInput') newEmailInput?: ElementRef<HTMLInputElement>;

  currentPage: number = 1;
  showAddInput = false;
  newEmail = '';

  constructor(
    private dialog: MatDialog,
    private emailService: EmailService,
    private snackBar: MatSnackBar
  ) {}

  ngAfterViewInit() {
    if (this.showAddInput && this.newEmailInput) {
      this.newEmailInput.nativeElement.focus();
    }
  }

  get totalPages(): number {
    return Math.ceil(this.emails.length / this.pageSize);
  }

  get paginatedEmails(): EmailDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.emails.slice(start, start + this.pageSize);
  }

  previousPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  addEmail(): void {
    const email = this.newEmail.trim();
    if (!email) return;

    this.emailService.addEmail(this.emailType, this.entiteId, email).subscribe({
      next: (newEmail) => {
        this.emails = [...this.emails, newEmail];
        this.emailAdded.emit(newEmail);
        this.snackBar.open('Email ajouté', 'Fermer', { duration: 2000 });
        this.newEmail = '';
        this.showAddInput = false;
        this.currentPage = this.totalPages; // va à la dernière page
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
    const index = this.emails.findIndex(e => e.id === updatedEmail.id);
    if (index !== -1) this.emails[index] = updatedEmail;
    this.emailUpdated.emit(updatedEmail);
  }

  onEmailDeleted(id: number): void {
    this.emails = this.emails.filter(e => e.id !== id);
    this.emailDeleted.emit(id);
    if (this.paginatedEmails.length === 0 && this.currentPage > 1) {
      this.currentPage--;
    }
  }

  openViewAllModal(): void {
    const dialogRef = this.dialog.open(EmailListDialog, {
      width: '600px',
      data: {
        emails: this.emails,
        emailType: this.emailType,
        entiteId: this.entiteId,
        isCreateur: this.isCreateur
      }
    });

    dialogRef.componentInstance.emailAdded.subscribe((email: EmailDto) => {
      this.emails = [...this.emails, email];
      this.emailAdded.emit(email);
    });
    dialogRef.componentInstance.emailUpdated.subscribe((email: EmailDto) => {
      const index = this.emails.findIndex(e => e.id === email.id);
      if (index !== -1) this.emails[index] = email;
      this.emailUpdated.emit(email);
    });
    dialogRef.componentInstance.emailDeleted.subscribe((id: number) => {
      this.emails = this.emails.filter(e => e.id !== id);
      this.emailDeleted.emit(id);
    });
  }
}