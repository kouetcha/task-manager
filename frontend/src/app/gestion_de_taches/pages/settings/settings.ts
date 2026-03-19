// settings.component.ts
import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

import { UpdateUserDto } from '../../interfaces/UserInterface';
import { User } from '../../models/user';
import { ThemeMode, ThemeService } from '../../services/ThemeService';
import { UserService } from '../../services/UserService';
import { AuthService } from '../../services/AuthService';

@Component({
  selector:    'app-settings',
  standalone:  true,
  templateUrl: './settings.html',
  styleUrls:   ['./settings.css'],
  imports:     [CommonModule, FormsModule, ReactiveFormsModule]
})
export class Settings implements OnInit {

  private fb          = inject(FormBuilder);
  private themeService = inject(ThemeService);
  private userService  = inject(UserService);
  private authService  = inject(AuthService);
  private breakpoint  = inject(BreakpointObserver);

  user = signal<User | null>(null);

  // ── État ─────────────────────────────────────────────────────
  currentUser   = signal<User | null>(null);
  activeSection = signal<'profil' | 'apparence' | 'securite' | 'notifications'>('profil');
  isSaving      = signal(false);
  saveSuccess   = signal(false);
  saveError     = signal<string | null>(null);
  previewUrl    = signal<string | null>(null);
  isMobile      = signal<boolean>(false);
  

  // ── Thème ────────────────────────────────────────────────────
  currentTheme = this.themeService.currentMode;

  readonly themes: { value: ThemeMode; label: string; icon: string; desc: string }[] = [
    { value: 'light',  label: 'Clair',   icon: '☀️',  desc: 'Toujours en mode clair'       },
    { value: 'dark',   label: 'Sombre',  icon: '🌙',  desc: 'Toujours en mode sombre'      },
    { value: 'system', label: 'Système', icon: '💻',  desc: 'Suit les préférences système' },
  ];

  // ── Formulaire profil ────────────────────────────────────────
  profilForm!: FormGroup;

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.user.set(user);
      this.currentUser.set(user);
      this.initForm(user!);
    });

    // Observer la taille d'écran
    this.breakpoint.observe([Breakpoints.HandsetPortrait, Breakpoints.HandsetLandscape])
      .subscribe(result => {
        this.isMobile.set(result.matches);
      });
  }

  private initForm(user: User): void {
    this.profilForm = this.fb.group({
      nom:       [user.nom,       [Validators.required, Validators.minLength(2)]],
      prenom:    [user.prenom,    [Validators.required, Validators.minLength(2)]],
      email:     [user.email,     [Validators.required, Validators.email]],
      telephone: [user.telephone, [Validators.pattern(/^\+?[0-9\s\-]{6,15}$/)]],
    });
  }

  // ── Navigation sections ──────────────────────────────────────
  setSection(section: 'profil' | 'apparence' | 'securite' | 'notifications'): void {
    this.activeSection.set(section);
    this.resetFeedback();
    
    // Sur mobile, scroller vers le haut
    if (this.isMobile()) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // ── Thème ────────────────────────────────────────────────────
  setTheme(mode: ThemeMode): void {
    this.themeService.setTheme(mode);
  }

  // ── Photo de profil ──────────────────────────────────────────
  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    // Vérifier la taille du fichier (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      this.saveError.set('La photo ne doit pas dépasser 5MB');
      return;
    }

    // Vérifier le type de fichier
    if (!file.type.startsWith('image/')) {
      this.saveError.set('Le fichier doit être une image');
      return;
    }

    // Preview local immédiat
    const reader = new FileReader();
    reader.onload = () => this.previewUrl.set(reader.result as string);
    reader.readAsDataURL(file);

    // Upload
    const userId = this.currentUser()!.id;
    this.userService.uploadProfilePicture(userId, file).subscribe({
      next: res => {
        this.currentUser.set(res);
        this.authService.setUser(res);
        this.showSuccess('Photo de profil mise à jour');
      },
      error: (err) => {
        this.saveError.set('Erreur lors du téléchargement de la photo.');
        console.error('Upload error:', err);
      }
    });
  }

  // ── Sauvegarde profil ────────────────────────────────────────
  saveProfile(): void {
    if (this.profilForm.invalid) {
      this.profilForm.markAllAsTouched();
      
      // Sur mobile, scroller vers la première erreur
      if (this.isMobile()) {
        setTimeout(() => {
          const firstError = document.querySelector('.border-red-400');
          firstError?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 100);
      }
      return;
    }

    this.isSaving.set(true);
    this.resetFeedback();

    const userId = this.currentUser()!.id;
    const dto: UpdateUserDto = this.profilForm.value;

    this.userService.update(userId, dto).subscribe({
      next: updated => {
        this.currentUser.set(updated);
        this.authService.setUser(updated);
        this.isSaving.set(false);
        this.showSuccess('Profil mis à jour avec succès');
      },
      error: (err) => {
        this.isSaving.set(false);
        this.saveError.set('Une erreur est survenue. Veuillez réessayer.');
        console.error('Update error:', err);
      }
    });
  }

  // ── Helpers ──────────────────────────────────────────────────
  get avatarUrl(): string {
    return this.previewUrl()
      ?? this.currentUser()?.profilePictureLink
      ?? this.getInitialsAvatar();
  }

  private getInitialsAvatar(): string {
    const u = this.currentUser();
    if (!u) return '';
    
    const initials = `${u.prenom?.[0] ?? ''}${u.nom?.[0] ?? ''}`.toUpperCase();
    
    // Taille adaptative
    const size = this.isMobile() ? 64 : 80;
    const fontSize = this.isMobile() ? 22 : 28;
    
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">
      <rect width="${size}" height="${size}" rx="${size/2}" fill="#4F46E5"/>
      <text x="50%" y="54%" dominant-baseline="middle" text-anchor="middle"
            fill="white" font-size="${fontSize}" font-family="sans-serif">${initials}</text>
    </svg>`;
    
    return `data:image/svg+xml;base64,${btoa(svg)}`;
  }

  private showSuccess(message?: string): void {
    this.saveSuccess.set(true);
    setTimeout(() => this.saveSuccess.set(false), 3000);
  }

  private resetFeedback(): void {
    this.saveSuccess.set(false);
    this.saveError.set(null);
  }

  hasError(field: string): boolean {
    const ctrl = this.profilForm?.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  // Getter pour le texte du bouton
  get saveButtonText(): string {
    if (this.isSaving()) return 'Enregistrement...';
    return this.isMobile() ? 'Enregistrer' : 'Enregistrer les modifications';
  }

  showFullView = signal<boolean>(false);
fullViewImage = signal<string>('');


openFullView(): void {
  this.fullViewImage.set(this.avatarUrl);
  this.showFullView.set(true);
  
  // Empêcher le scroll du body
  document.body.style.overflow = 'hidden';
}

closeFullView(): void {
  this.showFullView.set(false);
  this.fullViewImage.set('');
  
  // Réactiver le scroll
  document.body.style.overflow = '';
}

ngOnDestroy(): void {

  document.body.style.overflow = '';
}


@HostListener('document:keydown.escape')
  onEscapePress() {
  if (this.showFullView()) {
    this.closeFullView();
  }
}
}