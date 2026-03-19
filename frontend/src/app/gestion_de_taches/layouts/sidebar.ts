import { Component, inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { SidebarService } from '../services/SidebarService';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

import { Subscription } from 'rxjs';
import { User } from '../models/user'; // à définir selon votre modèle
import { AuthService } from '../services/AuthService';
import { DashboardService } from '../services/dashboard.service';
import { MenuStats } from '../models/dashboard.model';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit, OnDestroy {
  sidebarService = inject(SidebarService);
  authService = inject(AuthService);
  cdr=inject(ChangeDetectorRef)

  menuStats: MenuStats={
    nbreProjets:0,
    nbrActivites:0,
    nbrTaches:0
  }

  constructor(private dashBoardService: DashboardService){
     this.dashBoardService.getMenuStats().subscribe((menu)=>{
      this.menuStats=menu
      this.cdr.detectChanges()
     })
  }
 
  onAvatarError(event: Event): void {
  if (this.user) {
    this.user = { ...this.user, profilePictureLink: '' };
  }
}


  user: User | null = null;
  private userSub: Subscription | null = null;

  ngOnInit(): void {
    this.userSub = this.authService.user$.subscribe((user) => {
      this.user = user;
      if(this.sidebarService.isDesktop())
      {
      this.sidebarService.open()
      }
      this.cdr.detectChanges()
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