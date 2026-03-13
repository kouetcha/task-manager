import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';

import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { CreateUserDto } from '../../interfaces/UserInterface';
import { UserCategory } from '../../models/user';
import { UserService } from '../../services/UserService';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatSnackBarModule],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class Register implements OnInit {
  registerForm!: FormGroup;
  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;
  passwordStrength = 0;

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UserService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      roleProjet: ['', Validators.required],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\+\d{7,15}$/)]],
      motdepasse: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmMotdepasse: ['', Validators.required],
      acceptTerms: [false, Validators.requiredTrue]
    }, { validators: this.passwordMatchValidator });

    // Écouter les changements du mot de passe pour calculer la force
    this.registerForm.get('motdepasse')?.valueChanges.subscribe(() => {
      this.calculatePasswordStrength();
    });
  }

  // Validateur personnalisé pour le mot de passe
  private passwordValidator(control: AbstractControl) {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumber = /\d/.test(value);
    const hasMinLength = value.length >= 8;

    const errors: any = {};
    if (!hasUpperCase) errors.missingUpperCase = true;
    if (!hasLowerCase) errors.missingLowerCase = true;
    if (!hasNumber) errors.missingNumber = true;
    if (!hasMinLength) errors.tooShort = true;

    return Object.keys(errors).length ? errors : null;
  }

  // Validateur pour la correspondance des mots de passe
  private passwordMatchValidator(form: FormGroup) {
    const password = form.get('motdepasse')?.value;
    const confirm = form.get('confirmMotdepasse')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  // Calcul de la force du mot de passe
  private calculatePasswordStrength(): void {
    const password = this.registerForm.get('motdepasse')?.value || '';
    let strength = 0;

    if (password.length >= 8) strength += 25;
    if (password.length >= 12) strength += 10;

    if (/[A-Z]/.test(password)) strength += 25;
    if (/[a-z]/.test(password)) strength += 25;
    if (/\d/.test(password)) strength += 25;
    if (/[^A-Za-z0-9]/.test(password)) strength += 25;

    this.passwordStrength = Math.min(strength, 100);
  }

  // Getters pour les erreurs de validation
  get emailError(): string {
    const emailControl = this.registerForm.get('email');
    if (emailControl?.hasError('required')) return 'L\'email est requis';
    if (emailControl?.hasError('email')) return 'Format d\'email invalide';
    return '';
  }

  get telephoneError(): string {
    const telephoneControl = this.registerForm.get('telephone');
    if (telephoneControl?.hasError('required')) return 'Le téléphone est requis';
    if (telephoneControl?.hasError('pattern')) return 'Format invalide (ex: +33612345678)';
    return '';
  }

  get passwordError(): string {
    const passwordControl = this.registerForm.get('motdepasse');
    if (passwordControl?.hasError('required')) return 'Le mot de passe est requis';
    if (passwordControl?.hasError('tooShort')) return 'Minimum 8 caractères';
    if (passwordControl?.hasError('missingUpperCase')) return 'Ajoutez une majuscule';
    if (passwordControl?.hasError('missingLowerCase')) return 'Ajoutez une minuscule';
    if (passwordControl?.hasError('missingNumber')) return 'Ajoutez un chiffre';
    return '';
  }

  // Getters pour les indicateurs de force
  get hasMinLength(): boolean {
    const password = this.registerForm.get('motdepasse')?.value || '';
    return password.length >= 8;
  }

  get hasUpperCase(): boolean {
    const password = this.registerForm.get('motdepasse')?.value || '';
    return /[A-Z]/.test(password);
  }

  get hasLowerCase(): boolean {
    const password = this.registerForm.get('motdepasse')?.value || '';
    return /[a-z]/.test(password);
  }

  get hasNumber(): boolean {
    const password = this.registerForm.get('motdepasse')?.value || '';
    return /\d/.test(password);
  }

  // Classe CSS pour la barre de force
  getPasswordStrengthClass(): string {
    if (this.passwordStrength < 40) return 'weak';
    if (this.passwordStrength < 70) return 'medium';
    return 'strong';
  }

  // Texte pour la force du mot de passe
  getPasswordStrengthText(): string {
    if (this.passwordStrength < 40) return 'Faible';
    if (this.passwordStrength < 70) return 'Moyen';
    return 'Fort';
  }

  // Basculer la visibilité du mot de passe
  togglePasswordVisibility(field: 'motdepasse' | 'confirmMotdepasse'): void {
    if (field === 'motdepasse') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  // Soumission du formulaire
  submit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      
      // Focus sur le premier champ invalide
      const invalidControl = this.findInvalidControl();
      if (invalidControl) {
        invalidControl.focus();
      }
      
      return;
    }

    this.isLoading = true;
    const { confirmMotdepasse, acceptTerms, ...formValue } = this.registerForm.value;

    const dto: CreateUserDto = {
      email: formValue.email,
      telephone: formValue.telephone,
      nom: formValue.nom,
      prenom: formValue.prenom,
      category: formValue.category as UserCategory,
      motdepasse: formValue.motdepasse,
      roleProjet:formValue.roleProjet
    };

    this.utilisateurService.create(dto).subscribe({
      next: (user) => {
        this.showSuccessMessage(user.prenom);
        this.registerForm.reset();
        this.router.navigate(['/login'], {
          queryParams: { registered: 'true' }
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur d\'inscription:', error);
        this.showErrorMessage(error);
        this.isLoading = false;
      }
    });
  }

  // Marquer tous les champs comme touchés
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Trouver le premier contrôle invalide
  private findInvalidControl(): HTMLElement | null {
    const formElements = document.querySelectorAll('.form-input, .form-select');
    for (const element of Array.from(formElements)) {
      const inputElement = element as HTMLElement;
      const id = inputElement.id;
      const control = this.registerForm.get(id);
      if (control?.invalid && control?.touched) {
        return inputElement;
      }
    }
    return null;
  }

  // Afficher le message de succès
  private showSuccessMessage(username: string): void {
    this.snackBar.open(
      `🎉 Félicitations ${username} ! Votre compte a été créé avec succès.`,
      'Fermer',
      {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['snackbar-success']
      }
    );
  }

  // Afficher le message d'erreur
  private showErrorMessage(error: any): void {
    let message = '❌ Erreur lors de la création du compte';
    
    if (error.status === 409) {
      message = '❌ Cet email est déjà utilisé';
    } else if (error.status === 400) {
      message = '❌ Données invalides';
    } else if (error.status === 0) {
      message = '❌ Impossible de se connecter au serveur';
    } else if (error.error?.message) {
      message = `❌ ${error.error.message}`;
    }

    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      panelClass: ['snackbar-error']
    });
  }
}