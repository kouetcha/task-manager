// notification.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface NotificationItem {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = new BehaviorSubject<NotificationItem[]>([]);
  readonly notifications$ = this._notifications.asObservable();

  private defaultDuration = 4000;

  success(message: string, duration = this.defaultDuration): void {
    this.add('success', message, duration);
  }

  error(message: string, duration = this.defaultDuration): void {
    this.add('error', message, duration);
  }

  warning(message: string, duration = this.defaultDuration): void {
    this.add('warning', message, duration);
  }

  info(message: string, duration = this.defaultDuration): void {
    this.add('info', message, duration);
  }

  dismiss(id: string): void {
    this._notifications.next(
      this._notifications.getValue().filter(n => n.id !== id)
    );
  }

  private add(type: NotificationItem['type'], message: string, duration: number): void {
    const id = crypto.randomUUID();
    const notif: NotificationItem = { id, type, message, duration };

    this._notifications.next([...this._notifications.getValue(), notif]);

    setTimeout(() => this.dismiss(id), duration);
  }
}