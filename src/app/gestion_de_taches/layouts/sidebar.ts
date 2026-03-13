import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { SidebarService } from '../services/SidebarService';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

import { Subscription } from 'rxjs';
import { User } from '../models/user'; // à définir selon votre modèle
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit, OnDestroy {
  sidebarService = inject(SidebarService);
  authService = inject(AuthService);

  user: User | null = null;
  private userSub: Subscription | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.user$.subscribe((user) => {
      this.user = user;
      if(this.sidebarService.isDesktop())
      {
      this.sidebarService.open()
      }
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

  onLinkClick() {
    // Fermer la sidebar uniquement sur mobile/tablette
    if (!this.sidebarService.isDesktop()) {
      this.sidebarService.close();
    }
  }
}