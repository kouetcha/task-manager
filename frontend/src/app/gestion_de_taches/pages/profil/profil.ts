// profil.component.ts
import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

import { User, UserCategory } from '../../models/user';
import { AuthService } from '../../services/AuthService';

@Component({
  selector:    'app-profil',
  standalone:  true,
  templateUrl: './profil.html',
  styleUrls:   ['./profil.css'],
  imports:     [CommonModule, RouterModule]
})
export class Profil implements OnInit {

  private authService = inject(AuthService);
  private router      = inject(Router);
  private breakpoint  = inject(BreakpointObserver);
  
showFullView = signal<boolean>(false);
fullViewImage = signal<string>('');

  user = signal<User | null>(null);
  isMobile = signal<boolean>(false);

  ngOnInit(): void {
    this.authService.user$.subscribe(u => this.user.set(u));
    
    // Observer la taille d'écran
    this.breakpoint.observe([Breakpoints.HandsetPortrait, Breakpoints.HandsetLandscape])
      .subscribe(result => {
        this.isMobile.set(result.matches);
      });
  }

  // ── Helpers ──────────────────────────────────────────────────
  get avatarUrl(): string {
    const u = this.user();
    if (u?.profilePictureLink) return u.profilePictureLink;
    return this.getInitialsAvatar();
  }

  get categoryLabel(): string {
    const cat = this.user()?.category;
    const labels: Record<string, string> = {
      [UserCategory.SUPER_ADMIN]: '👑 Super Admin',
      [UserCategory.ADMIN]:       '🛡️ Administrateur',
      [UserCategory.NORMAL]:      '👤 Utilisateur',
    };
    return cat ? (labels[cat] ?? cat) : '—';
  }

  // Version compacte pour mobile
  getCompactCategoryLabel(): string {
    if (!this.isMobile()) return this.categoryLabel;
    
    const cat = this.user()?.category;
    const compactLabels: Record<string, string> = {
      [UserCategory.SUPER_ADMIN]: '👑 Admin+',
      [UserCategory.ADMIN]:       '🛡️ Admin',
      [UserCategory.NORMAL]:      '👤 User',
    };
    return cat ? (compactLabels[cat] ?? cat) : '—';
  }

  get categoryColor(): string {
    const cat = this.user()?.category;
    const colors: Record<string, string> = {
      [UserCategory.SUPER_ADMIN]: 'bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-300',
      [UserCategory.ADMIN]:       'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
      [UserCategory.NORMAL]:      'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300',
    };
    return cat ? (colors[cat] ?? colors[UserCategory.NORMAL]) : '';
  }

  get statusColor(): string {
    return this.user()?.est_actif
      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300'
      : 'bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-300';
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return '—';
    
    const options: Intl.DateTimeFormatOptions = this.isMobile() 
      ? { day: '2-digit', month: '2-digit', year: 'numeric' } // Format court pour mobile
      : { day: '2-digit', month: 'long', year: 'numeric' };   // Format long pour desktop
      
    return new Date(date).toLocaleDateString('fr-FR', options);
  }

  formatDateTime(date: string | null | undefined): string {
    if (!date) return 'Jamais connecté';
    
    const options: Intl.DateTimeFormatOptions = this.isMobile()
      ? { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }
      : { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' };
      
    return new Date(date).toLocaleDateString('fr-FR', options);
  }

  // Version très compacte pour l'affichage dans la carte
  getCompactDateTime(date: string | null | undefined): string {
    if (!date) return 'Jamais';
    
    const now = new Date();
    const lastConn = new Date(date);
    const diffHours = Math.floor((now.getTime() - lastConn.getTime()) / (1000 * 60 * 60));
    
    // Si connexion récente (< 24h), afficher "Il y a X heures"
    if (diffHours < 24) {
      if (diffHours < 1) return 'À l\'instant';
      if (diffHours === 1) return 'Il y a 1h';
      return `Il y a ${diffHours}h`;
    }
    
    // Sinon afficher la date courte
    return lastConn.toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: '2-digit' 
    });
  }

  private getInitialsAvatar(): string {
    const u = this.user();
    if (!u) return '';
    
    const initials = `${u.prenom?.[0] ?? ''}${u.nom?.[0] ?? ''}`.toUpperCase();
    
    // Taille adaptative pour mobile/desktop
    const size = this.isMobile() ? 64 : 96;
    const fontSize = this.isMobile() ? 24 : 32;
    
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">
      <rect width="${size}" height="${size}" rx="${size/2}" fill="#4F46E5"/>
      <text x="50%" y="54%" dominant-baseline="middle" text-anchor="middle"
            fill="white" font-size="${fontSize}" font-family="sans-serif">${initials}</text>
    </svg>`;
    
    return `data:image/svg+xml;base64,${btoa(svg)}`;
  }

  goToSettings(): void {
    this.router.navigate(['/app/settings']);
  }



// Ajoutez ces méthodes
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

// Gestion de la touche Echap
@HostListener('document:keydown.escape')
onEscapePress() {
  if (this.showFullView()) {
    this.closeFullView();
  }
}

// Dans ngOnDestroy (à ajouter si vous ne l'avez pas)
ngOnDestroy(): void {
  // Nettoyer au cas où le modal reste ouvert
  document.body.style.overflow = '';
}
}