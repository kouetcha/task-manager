import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { NotificationItem, NotificationService } from '../../../services/notification.service';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-custom-notification',
  imports: [CommonModule],
  templateUrl: './custom-notification.html',
  styleUrl: './custom-notification.css',
})
export class CustomNotification implements OnInit, OnDestroy {
  notifications: NotificationItem[] = [];
  private sub!: Subscription;

  constructor(
    private notifService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sub = this.notifService.notifications$.subscribe(notifs => {
      this.notifications = notifs;
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  dismiss(notif: NotificationItem): void {
    this.notifService.dismiss(notif.id);
  }

  getLabel(type: string): string {
    return ({ success: 'Succès', error: 'Erreur', warning: 'Attention', info: 'Info' } as Record<string, string>)[type] ?? type;
  }

  trackById(_: number, n: NotificationItem): string { return n.id; }
}