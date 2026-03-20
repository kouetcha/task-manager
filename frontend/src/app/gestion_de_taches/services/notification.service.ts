// notification.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface NotificationItem {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info'|'message';
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
  message(message: string, duration = this.defaultDuration): void {
    this.add('message', message, duration);
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
    const id = this.generateId();
    const notif: NotificationItem = { id, type, message, duration };

    this._notifications.next([...this._notifications.getValue(), notif]);

    setTimeout(() => this.dismiss(id), duration);
  }

  private generateId(): string {
  // crypto.randomUUID disponible en HTTPS/localhost uniquement
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }

  // Fallback universel — fonctionne partout (HTTP, mobile, vieux navigateurs)
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0;
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}
}