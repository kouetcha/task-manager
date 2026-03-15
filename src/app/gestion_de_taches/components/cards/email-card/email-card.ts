import {
  Component, Input, Output, EventEmitter, ViewChild, ElementRef,
  AfterViewInit, OnDestroy, TemplateRef, ViewContainerRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Overlay, OverlayRef, OverlayModule } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { Subscription } from 'rxjs';

import { EmailDto, MailTYPE } from '../../../interfaces/base-entity-gestion';
import { EmailService } from '../../../services/emails/email-service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-email-card',
  standalone: true,
  imports: [CommonModule, FormsModule, OverlayModule],
  templateUrl: './email-card.html',
})
export class EmailCard implements AfterViewInit, OnDestroy {
  @Input() emailDto!: EmailDto;
  @Input() emailType!: MailTYPE;
  @Input() isCreateur!: boolean;
  @Output() update = new EventEmitter<EmailDto>();
  @Output() delete = new EventEmitter<number>();
  @Output() toggleActive = new EventEmitter<EmailDto>();
  

  // Édition
  editing = false;
  editedEmail = '';
  isProcessing = false;
  private isClickingButton = false;

  // Menu
  actionVisible = false;
  @ViewChild('menuTrigger') menuTrigger!: ElementRef;
  @ViewChild('menuTemplate') menuTemplate!: TemplateRef<any>;
  private overlayRef: OverlayRef | null = null;
  private backdropClickSub: Subscription | null = null;

  @ViewChild('emailInput') emailInput?: ElementRef<HTMLInputElement>;

  constructor(
    private emailService: EmailService,
    private snackBar: MatSnackBar,
    private overlay: Overlay,
    private viewContainerRef: ViewContainerRef
  ) {}

  ngAfterViewInit() {
    if (this.editing && this.emailInput) {
      this.emailInput.nativeElement.focus();
    }
  }

  ngOnDestroy() {
    this.closeMenu();
  }

  // ========== Gestion du menu ==========
  toggleActions(event?: MouseEvent) {
    if (event) event.stopPropagation();
    if (this.actionVisible) this.closeMenu();
    else this.openMenu();
  }

  private openMenu() {
    if (this.overlayRef) return;

    const positionStrategy = this.overlay.position()
      .flexibleConnectedTo(this.menuTrigger.nativeElement)
      .withPositions([
        { originX: 'end', originY: 'bottom', overlayX: 'end', overlayY: 'top' }, // en bas
        { originX: 'end', originY: 'top', overlayX: 'end', overlayY: 'bottom' }, // en haut
      ]);

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.close(),
      hasBackdrop: true,
      backdropClass: 'cdk-overlay-transparent-backdrop',
    });

    const portal = new TemplatePortal(this.menuTemplate, this.viewContainerRef);
    this.overlayRef.attach(portal);

    this.backdropClickSub = this.overlayRef.backdropClick().subscribe(() => this.closeMenu());
    this.actionVisible = true;
  }

  closeMenu() {
    if (this.overlayRef) {
      this.overlayRef.detach();
      this.overlayRef = null;
    }
    this.backdropClickSub?.unsubscribe();
    this.actionVisible = false;
  }

  // ========== Édition ==========
  startEdit() {
    this.editedEmail = this.emailDto.email;
    this.editing = true;
    this.closeMenu();
    setTimeout(() => this.emailInput?.nativeElement.focus());
  }

  onInputBlur() {
    if (this.isClickingButton) return;
    this.cancelEdit();
  }

  onButtonMousedown() {
    this.isClickingButton = true;
  }

  saveEdit() {
    this.isClickingButton = false;
    if (this.isProcessing) return;

    const newEmail = this.editedEmail?.trim();
    if (!newEmail || newEmail === this.emailDto.email) {
      this.cancelEdit();
      return;
    }

    this.isProcessing = true;
    this.emailService.updateEmail(this.emailType, this.emailDto.entiteId, this.emailDto.id, newEmail)
      .subscribe({
        next: (updatedEmail) => {
          this.emailDto = updatedEmail;
          this.update.emit(updatedEmail);
          this.snackBar.open('Email mis à jour', 'Fermer', { duration: 2000 });
          this.editing = false;
          this.isProcessing = false;
        },
        error: (err) => {
          console.error('Update failed', err);
          this.snackBar.open('Erreur lors de la mise à jour', 'Fermer', { duration: 3000 });
          this.isProcessing = false;
        }
      });
  }

  cancelEdit() {
    this.isClickingButton = false;
    this.editing = false;
    this.editedEmail = '';
  }

  // ========== Actions ==========
  onDelete() {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet email ?')) {
      this.isProcessing = true;
      this.emailService.removeEmail(this.emailType, this.emailDto.entiteId, this.emailDto.id)
        .subscribe({
          next: () => {
            this.delete.emit(this.emailDto.id);
            this.snackBar.open('Email supprimé', 'Fermer', { duration: 2000 });
            this.closeMenu();
            this.isProcessing = false;
          },
          error: (err) => {
            console.error('Delete failed', err);
            this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
            this.isProcessing = false;
          }
        });
    }
  }

  onToggleActive() {
    this.isProcessing = true;
    const action = this.emailDto.active
      ? this.emailService.deactivateEmail(this.emailType, this.emailDto.entiteId, this.emailDto.id)
      : this.emailService.activateEmail(this.emailType, this.emailDto.entiteId, this.emailDto.id);

    action.subscribe({
      next: (updatedEmail) => {
        this.emailDto = updatedEmail;
        this.toggleActive.emit(updatedEmail);
        this.snackBar.open('Statut modifié', 'Fermer', { duration: 2000 });
        this.closeMenu();
        this.isProcessing = false;
      },
      error: (err) => {
        console.error('Toggle active failed', err);
        this.snackBar.open('Erreur lors du changement de statut', 'Fermer', { duration: 3000 });
        this.isProcessing = false;
      }
    });
  }

  get activeButtonLabel(): string {
    return this.emailDto.active ? 'Désactiver' : 'Activer';
  }
}