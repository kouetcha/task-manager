import { Injectable } from '@angular/core';
import { Page } from '../interfaces/generals';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationsService {

  private apiUrl = environment.API_URL + '/tasksmanager/notifications';

  constructor(private http: HttpClient) {}

  // 🔴 Badge notifications
  private unseenCount$ = new BehaviorSubject<number>(0);

  getUnseenCount$(): Observable<number> {
    return this.unseenCount$.asObservable();
  }

  refreshUnseenCount(userId: number): void {
    this.countUnseen(userId).subscribe(count => {
      this.unseenCount$.next(count);
    });
  }

  // 🔹 Helper params (DRY)
  private buildParams(page: number, size: number, sort: string): HttpParams {
    return new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
  }

  // 🔹 Notifications paginées
  getNotifications(
    userId: number,
    page: number = 0,
    size: number = 10,
    sort: string = 'date,desc'
  ): Observable<Page<Notification>> {

    const params = this.buildParams(page, size, sort);

    return this.http.get<Page<Notification>>(
      `${this.apiUrl}/${userId}/page`,
      { params }
    );
  }

  // 🔹 Notifications non lues paginées
  getUnseenNotifications(
    userId: number,
    page: number = 0,
    size: number = 10,
    sort: string = 'date,desc'
  ): Observable<Page<Notification>> {

    const params = this.buildParams(page, size, sort);

    return this.http.get<Page<Notification>>(
      `${this.apiUrl}/${userId}/unseen/page`,
      { params }
    );
  }

  // 🔹 Non lues (les marque comme lues côté backend)
  getUnseen(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/${userId}/unseen`);
  }

  // 🔹 Count non lues (badge)
  countUnseen(userId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${userId}/unseen/count`);
  }
}