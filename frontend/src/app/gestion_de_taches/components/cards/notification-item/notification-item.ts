import { Component, Input } from '@angular/core';
import { Notification } from '../../../models/notification.model';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-notification-item',
  imports: [DatePipe],
  templateUrl: './notification-item.html',
  styleUrl: './notification-item.css',
})
export class NotificationItem {
  @Input() notification!: Notification;
}
