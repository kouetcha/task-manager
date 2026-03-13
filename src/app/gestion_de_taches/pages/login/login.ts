import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoginDto } from '../../interfaces/UserInterface';

import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs/operators';

import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../services/UserService';
import { AuthService } from '../../services/AuthService';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class Login {
  loginForm!: FormGroup;
  showPassword = false;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UserService,
    private snackBar: MatSnackBar,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motdepasse: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false]
    });

    // Remplissage automatique depuis le localStorage si "Se souvenir de moi"
    const savedEmail = localStorage.getItem('rememberedEmail');
    if (savedEmail) {
      this.loginForm.patchValue({ email: savedEmail, rememberMe: true });
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
    const passwordField = document.getElementById('password') as HTMLInputElement;
    if (passwordField) {
      passwordField.type = this.showPassword ? 'text' : 'password';
    }
  }

  submit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.isLoading = true;
    const formValue = this.loginForm.value;

    // Sauvegarder l'email si "Se souvenir de moi" est coché
    if (formValue.rememberMe) {
      localStorage.setItem('rememberedEmail', formValue.email);
    } else {
      localStorage.removeItem('rememberedEmail');
    }

    const dto: LoginDto = {
      email: formValue.email,
      motdepasse: formValue.motdepasse
    };

    this.utilisateurService.login(dto).pipe(
      switchMap((auth: { email: string; token: string }) => {
        localStorage.setItem('token', auth.token);
        localStorage.setItem('email', auth.email);
        
        return this.utilisateurService.getByEmail(auth.email);
      })
    ).subscribe({
      next: (user) => {
        this.authService.setUser(user);
        this.showSuccessMessage(user.prenom);
        this.loginForm.reset({ rememberMe: this.loginForm.value.rememberMe });
        this.router.navigateByUrl('app/dashboard');
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur de connexion:', error);
        this.showErrorMessage(error);
        this.isLoading = false;
      }
    });
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  private showSuccessMessage(username: string): void {
    this.snackBar.open(
      `✅ Bienvenue ${username} ! Connexion réussie.`,
      'Fermer',
      {
        duration: 4000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['snackbar-success']
      }
    );
  }

  private showErrorMessage(error: any): void {
    let message = '❌ Erreur de connexion';
    
    if (error.status === 401) {
      message = '❌ Email ou mot de passe incorrect';
    } else if (error.status === 0) {
      message = '❌ Impossible de se connecter au serveur';
    } else if (error.status === 429) {
      message = '❌ Trop de tentatives de connexion. Veuillez réessayer plus tard';
    }

    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      panelClass: ['snackbar-error']
    });
  }

  get emailError(): string {
    const emailControl = this.loginForm.get('email');
    if (emailControl?.hasError('required')) return 'L\'email est requis';
    if (emailControl?.hasError('email')) return 'Email invalide';
    return '';
  }

  get passwordError(): string {
    const passwordControl = this.loginForm.get('motdepasse');
    if (passwordControl?.hasError('required')) return 'Le mot de passe est requis';
    if (passwordControl?.hasError('minlength')) return 'Minimum 8 caractères';
    return '';
  }
}