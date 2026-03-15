import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ChangePasswordDto, CreateUserDto, LoginDto, UpdateUserDto } from '../interfaces/UserInterface';
import { environment } from '../../../environments/environment';
import { User } from '../models/user';


@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly API_URL = environment.API_URL+'/tasksmanager/utilisateur';


  constructor(private http: HttpClient) {}

  // 🔹 GET /utilisateur
  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.API_URL);
  }

  // 🔹 GET /utilisateur/{id}
  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/${id}`);
  }

  // 🔹 GET /utilisateur/email/{email}
  getByEmail(email: string): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/email/${email}`);
  }

  // 🔹 POST /utilisateur
  create(dto: CreateUserDto): Observable<User> {
    return this.http.post<User>(`${this.API_URL}/create`, dto);
  }

  // 🔹 PATCH /utilisateur/{id}
  update(id: number, dto: UpdateUserDto): Observable<User> {
    return this.http.patch<User>(`${this.API_URL}/${id}`, dto);
  }

  // 🔹 DELETE /utilisateur/{id}
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
  // 🔹 PATCH /utilisateur/{id}/profil-picture - Upload photo de profil
  uploadProfilePicture(id: number, file: File): Observable<{ imageUrl: string }> {
    const formData = new FormData();
    formData.append('image', file);
    
    return this.http.patch<{ imageUrl: string }>(
      `${this.API_URL}/${id}/profil-picture`,
      formData
    );
  }
  // 🔹 PATCH /utilisateur/{id}/etat
  toggleEtat(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/etat`, {});
  }

  // 🔹 PATCH /utilisateur/motdepasse
  changePassword(dto: ChangePasswordDto): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/motdepasse`, dto);
  }

  // 🔹 PATCH /utilisateur/connexion
  login(dto: LoginDto): Observable<any> {
    return this.http.patch<any>(`${this.API_URL}/connexion`, dto);
  }
}

