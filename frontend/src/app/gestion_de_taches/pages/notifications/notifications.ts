import { Component, inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { NotificationsService } from '../../services/notifications.service';
import { AuthService } from '../../services/AuthService';
import { Notification } from '../../models/notification.model';
import { MatIcon } from '@angular/material/icon';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, MatIcon],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
})
export class Notifications implements OnInit, OnDestroy {

  private notifService = inject(NotificationsService);
  private authService  = inject(AuthService);
  private popService   = inject(NotificationService);
  private cdr          = inject(ChangeDetectorRef);
  private destroy$     = new Subject<void>();

  notifications: Notification[] = [];
  filter:    'all' | 'unseen' = 'all';
  page       = 0;
  size       = 15;
  isLastPage = false;
  isLoading  = false;

  // Sélection
  selectedIds = new Set<number>();

  get allSelected(): boolean {
    return this.notifications.length > 0 &&
           this.notifications.every(n => this.selectedIds.has(n.id));
  }

  get someSelected(): boolean {
    return this.selectedIds.size > 0;
  }

  get selectedUnseenIds(): number[] {
    return this.notifications
      .filter(n => this.selectedIds.has(n.id) && !n.seen)
      .map(n => n.id);
  }

  get selectedIdsList(): number[] {
    return Array.from(this.selectedIds);
  }

  ngOnInit(): void {
    this.loadPage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  hasUnseen(): boolean {
    return this.notifications.some(noti => !noti.seen);
  }

  loadPage(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading  = true;
    this.selectedIds.clear();

    const request$ = this.filter === 'all'
      ? this.notifService.getNotifications(userId, this.page, this.size)
      : this.notifService.getUnseenNotifications(userId, this.page, this.size);

    request$.pipe(takeUntil(this.destroy$)).subscribe({
      next: page => {
        this.notifications = page.content;
        this.isLastPage    = page.page.totalPages - 1 <= page.page.number;
        this.isLoading     = false;
        this.cdr.detectChanges();
      },
      error: () => { 
        this.isLoading = false;
        this.popService.message('Erreur lors du chargement des notifications');
      }
    });
  }

  setFilter(filter: 'all' | 'unseen'): void {
    this.filter = filter;
    this.page   = 0;
    this.loadPage();
  }

  nextPage(): void {
    if (!this.isLastPage) { 
      this.page++; 
      this.loadPage(); 
    }
  }

  prevPage(): void {
    if (this.page > 0) { 
      this.page--; 
      this.loadPage(); 
    }
  }

  // --- Sélection ---

  toggleOne(id: number): void {
    this.selectedIds.has(id)
      ? this.selectedIds.delete(id)
      : this.selectedIds.add(id);
  }

  toggleAll(): void {
    if (this.allSelected) {
      this.selectedIds.clear();
    } else {
      this.notifications.forEach(n => this.selectedIds.add(n.id));
    }
  }

  // --- Actions Marquer comme lu ---

  markSelectedAsSeen(): void {
    const ids = this.selectedUnseenIds;
    if (ids.length === 0) return;

    this.notifService.markAsSeen(ids).subscribe({
      next: () => {
        this.notifications = this.notifications.map(n =>
          ids.includes(n.id) ? { ...n, seen: true } : n
        );
        this.selectedIds.clear();
        this.notifService.refreshUnseenCount(this.authService.currentUser!.id);
        this.popService.message(`${ids.length} notification(s) marquée(s) comme lue(s)`);
      },
      error: () => {
        this.popService.message('Erreur lors du marquage des notifications');
      }
    });
  }

  markAllAsSeen(): void {
    this.notifService.markAllAsSeen().subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, seen: true }));
        this.selectedIds.clear();
        this.popService.message('Toutes les notifications ont été marquées comme lues');
      },
      error: () => {
        this.popService.message('Erreur lors du marquage des notifications');
      }
    });
  }

  // --- Actions Supprimer ---

  deleteOne(id: number): void {
    if (!confirm('Voulez-vous vraiment supprimer cette notification ?')) {
      return;
    }

    // Appel direct avec un tableau contenant un seul ID
    this.notifService.deleteNotification([id]).subscribe({
      next: () => {
        // Supprimer de la liste locale
        this.notifications = this.notifications.filter(n => n.id !== id);
        this.selectedIds.delete(id);
        
        // Rafraîchir le compteur
        this.notifService.refreshUnseenCount(this.authService.currentUser!.id);
        
        // Message de confirmation
        this.popService.message('Notification supprimée');
        
        // Recharger si la page est vide
        if (this.notifications.length === 0 && this.page > 0) {
          this.page--;
          this.loadPage();
        }
        
        this.cdr.detectChanges();
      },
      error: () => {
        this.popService.message('Erreur lors de la suppression');
      }
    });
  }

  deleteSelected(): void {
    const ids = this.selectedIdsList;
    if (ids.length === 0) return;

    const message = ids.length === 1 
      ? 'Voulez-vous vraiment supprimer cette notification ?'
      : `Voulez-vous vraiment supprimer ces ${ids.length} notifications ?`;

    if (!confirm(message)) {
      return;
    }

    this.isLoading = true;

    // Appel unique avec tous les IDs sélectionnés
    this.notifService.deleteNotification(ids).subscribe({
      next: () => {
        // Supprimer de la liste locale
        this.notifications = this.notifications.filter(n => !ids.includes(n.id));
        this.selectedIds.clear();
        
        // Rafraîchir le compteur
        this.notifService.refreshUnseenCount(this.authService.currentUser!.id);
        
        // Message de confirmation
        this.popService.message(`${ids.length} notification(s) supprimée(s)`);
        
        this.isLoading = false;
        
        // Recharger si la page est vide
        if (this.notifications.length === 0 && this.page > 0) {
          this.page--;
          this.loadPage();
        }
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors de la suppression :', error);
        this.popService.message('Erreur lors de la suppression des notifications');
        this.isLoading = false;
      }
    });
  }
}