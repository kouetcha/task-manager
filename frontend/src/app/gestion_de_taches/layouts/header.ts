import { ChangeDetectorRef, Component, ElementRef, HostListener, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SidebarService } from '../services/SidebarService';
import { AuthService } from '../services/AuthService';
import { ThemeMode, ThemeService } from '../services/ThemeService';
import { User } from '../models/user';
import { Subscription } from 'rxjs';




@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header  implements OnInit , OnDestroy{
   user:User|null=null;
    private userSubscription: Subscription = new Subscription();
  ngOnInit(): void {
  this.userSubscription = this.authService.user$.subscribe((user)=>{
      this.user = user;
      this.cdr.detectChanges()
   })
  }
   ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }
  sidebarService = inject(SidebarService);
  authService = inject(AuthService);
  themeService = inject(ThemeService);
  router=inject(Router);
  cdr=inject(ChangeDetectorRef)


  isUserMenuOpen = false;
  isThemeMenuOpen = false;

  @ViewChild('userMenuContainer') userMenuContainer!: ElementRef;
  @ViewChild('themeMenuContainer') themeMenuContainer!: ElementRef;

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  toggleThemeMenu() {
    this.isThemeMenuOpen = !this.isThemeMenuOpen;
  }

  setTheme(mode: ThemeMode) {
    this.themeService.setTheme(mode);
    this.isThemeMenuOpen = false;
  }

  // Fermer les menus si on clique à l'extérieur
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (this.isUserMenuOpen && this.userMenuContainer && !this.userMenuContainer.nativeElement.contains(event.target)) {
      this.isUserMenuOpen = false;
    }
    if (this.isThemeMenuOpen && this.themeMenuContainer && !this.themeMenuContainer.nativeElement.contains(event.target)) {
      this.isThemeMenuOpen = false;
    }
  }
onAvatarError(event: Event): void {
  if (this.user) {
    this.user = { ...this.user, profilePictureLink: '' };
  }
}
  logout() {
    // Implémentez votre logique de déconnexion
    console.log('Déconnexion');
    this.authService.logout()
     this.router.navigate(['/login']);
  }
}