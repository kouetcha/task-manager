import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private userSubject = new BehaviorSubject<User | null>(
    JSON.parse(localStorage.getItem('currentUser')!)
  );
  user$ = this.userSubject.asObservable();

  get token(): string | null {
    return localStorage.getItem('token');
  }
  
  getUser(): Observable<User | null> {
    return this.currentUserSubject.asObservable();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  private authStateSubject = new BehaviorSubject<boolean>(this.isLoggedIn);
authState$ = this.authStateSubject.asObservable();

  get currentUser(): User | null {
    return this.userSubject.value;
  }

  get isLoggedIn(): boolean {
    return !!this.token;
  }

  setUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.userSubject.next(user);
  }

  logout(): void {
    localStorage.clear();
    this.userSubject.next(null);
  }
}
