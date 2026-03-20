import {
  Component,
  ElementRef,
  HostListener,
  inject,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SidebarService } from '../services/SidebarService';
import { AuthService } from '../services/AuthService';
import { ThemeMode, ThemeService } from '../services/ThemeService';
import { User } from '../models/user';
import { Subject, takeUntil, filter, switchMap, BehaviorSubject } from 'rxjs';
import { WebSocketService } from '../services/websocket.service';
import { NotificationEvent } from '../services/websocket.service';
import { NotificationService } from '../services/notification.service';
import { NotificationsService } from '../services/notifications.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header implements OnInit, OnDestroy {

  user: User | null = null;
   unseenCount: number = 0;
  private destroy$ = new Subject<void>();

  sidebarService = inject(SidebarService);
  authService    = inject(AuthService);
  themeService   = inject(ThemeService);
  router         = inject(Router);
  wsService      = inject(WebSocketService);
  popService     = inject(NotificationService);
  notificationService=inject(NotificationsService);

  isUserMenuOpen  = false;
  isThemeMenuOpen = false;

  @ViewChild('userMenuContainer')  userMenuContainer!:  ElementRef;
  @ViewChild('themeMenuContainer') themeMenuContainer!: ElementRef;

  ngOnInit(): void {


    this.authService.user$
      .pipe(takeUntil(this.destroy$), filter(user => !!user))
      .subscribe(user => {
        this.user = user;
        this.notificationService.refreshUnseenCount(user.id);
      });

  
    this.authService.user$.pipe(
      takeUntil(this.destroy$),
      filter(user => !!user)
    ).subscribe(() => {
      this.wsService.connect();
     

        
    });

   
    this.wsService.connected$.pipe(
      takeUntil(this.destroy$),
      filter(connected => connected),        
      switchMap(() => this.wsService.onNotification()) 
    ).subscribe({
      next:  (event: NotificationEvent) => {
        console.log('🔔 Notification reçue :', event);
        this.popService.message(event.message);
          if (this.user) {
            this.notificationService.refreshUnseenCount(this.user.id);
          }
        
      },
      error: err => console.error('❌ Erreur WS :', err)
    });

     this.notificationService.getUnseenCount$().subscribe(count => {
      this.unseenCount = count;
    });

      this.notificationService.getUnseenCount$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.unseenCount = count;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.wsService.disconnect();
  }

  toggleUserMenu():  void { this.isUserMenuOpen  = !this.isUserMenuOpen;  }
  toggleThemeMenu(): void { this.isThemeMenuOpen = !this.isThemeMenuOpen; }

  setTheme(mode: ThemeMode): void {
    this.themeService.setTheme(mode);
    this.isThemeMenuOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isUserMenuOpen &&
        this.userMenuContainer &&
        !this.userMenuContainer.nativeElement.contains(event.target)) {
      this.isUserMenuOpen = false;
    }
    if (this.isThemeMenuOpen &&
        this.themeMenuContainer &&
        !this.themeMenuContainer.nativeElement.contains(event.target)) {
      this.isThemeMenuOpen = false;
    }
  }

  onAvatarError(): void {
    if (this.user) this.user = { ...this.user, profilePictureLink: '' };
  }

  logout(): void {
    this.wsService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}