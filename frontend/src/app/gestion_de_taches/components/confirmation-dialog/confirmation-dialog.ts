// src/app/gestion_de_taches/components/confirmation-dialog/confirmation-dialog.component.ts
import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-confirmation-dialog',
  template: `
    <div class="p-6">
      <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-2">
        {{ data.title }}
      </h2>
      <p class="text-gray-600 dark:text-gray-400 mb-6">
        {{ data.message }}
      </p>
      <div class="flex justify-end space-x-3">
        <button
          (click)="onCancel()"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"
        >
          {{ data.cancelText || 'Annuler' }}
        </button>
        <button
          (click)="onConfirm()"
          class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
        >
          {{ data.confirmText || 'Confirmer' }}
        </button>
      </div>
    </div>
  `
})
export class ConfirmationDialog {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialog>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      confirmText?: string;
      cancelText?: string;
    }
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}