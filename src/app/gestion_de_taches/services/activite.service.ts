// src/app/gestion_de_taches/services/activite.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environment';
import { Activite } from '../models/activite.model';
import { CreateActiviteDto, DateDto, TexteDto, UpdateActiviteDto } from '../interfaces/base-entity-gestion';
import { Page } from '../interfaces/generals';


@Injectable({
  providedIn: 'root'
})
export class ActiviteService {

  private readonly API_URL = environment.API_URL + '/tasksmanager'; // attention : le chemin complet est /projet/{projetId}/activites

  constructor(private http: HttpClient) {}

  /**
   * Récupère toutes les activités d'un projet
   */
  getByProjet(projetId: number): Observable<Activite[]> {
    return this.http.get<Activite[]>(`${this.API_URL}/activites/projet/${projetId}`);
  }

  /**
   * Récupère une activité par son ID
   */
  getById(id: number): Observable<Activite> {

    return this.http.get<Activite>(`${environment.API_URL}/tasksmanager/activites/${id}`);
  }
getMesActivites(
  email: string,
  page: number = 0,
  size: number = 10,
  sort: string = 'dateCreation,desc'
): Observable<Page<Activite>> {
  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sort', sort);

  return this.http.get<Page<Activite>>(
    `${this.API_URL}/activites/email/${email}`,
    { params }
  );
}
  

  /**
   * Crée une activité dans un projet
   * @param projetId ID du projet parent
   * @param dto Données de l'activité
   * @param fichiers Fichiers optionnels
   */
  create(projetId: number, dto: CreateActiviteDto, fichiers?: File[]): Observable<Activite> {
    const formData = this.buildFormData(dto, fichiers);
    return this.http.post<Activite>(`${this.API_URL}/activites/projet/${projetId}`, formData);
  }

  /**
   * Met à jour une activité (sans fichiers, en JSON)
   */
  update(id: number, dto: UpdateActiviteDto): Observable<Activite> {
    // Le PUT s'attend à un JSON, pas de FormData
    return this.http.put<Activite>(`${this.API_URL}/activites/${id}`, dto);
  }

  /**
   * Supprime une activité
   */
  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${environment.API_URL}/tasksmanager/activites/${id}`);
  }

  /**
   * Ajoute un fichier à une activité existante
   */
  addFichier(activiteId: number, file: File, nom?: string): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    if (nom) formData.append('nom', nom);
    return this.http.post<string>(`${environment.API_URL}/tasksmanager/activites/${activiteId}/fichiers`, formData);
  }

  /**
   * Supprime un fichier d'une activité
   */
  deleteFichier(activiteId: number, fichierId: number): Observable<string> {
    return this.http.delete<string>(`${environment.API_URL}/tasksmanager/activites/${activiteId}/fichiers/${fichierId}`);
  }

  /**
   * Construit un FormData à partir du DTO et des fichiers
   */
  private buildFormData(dto: CreateActiviteDto, fichiers?: File[]): FormData {
    const formData = new FormData();

    // Champs simples
    formData.append('designation', dto.designation);
    if (dto.description) formData.append('description', dto.description);
    formData.append('dateDebut', this.formatDateISO(dto.dateDebut));
    formData.append('dateFin', this.formatDateISO(dto.dateFin));
    formData.append('status', dto.status || 'EN_ATTENTE');
    formData.append('createurId', dto.createurId+'');

    // Emails sous forme JSON
    if (dto.emails && dto.emails.length > 0) {
       dto.emails.forEach(e => formData.append("emails", e));
      //formData.append('emails', JSON.stringify(dto.emails));
    }

    // Fichiers sous forme de liste indexée (comme dans ProjetService)
    if (fichiers && fichiers.length > 0) {
      fichiers.forEach((file, index) => {
        formData.append(`fichiers[${index}].nomFichier`, file.name);
        formData.append(`fichiers[${index}].fichier`, file);
      });
    }

    return formData;
  }

  private formatDateISO(date: Date | string | undefined): string {
    if (!date) return new Date().toISOString();
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toISOString();
  }

  updateActiviteDesignation( id: number, dto: TexteDto): Observable<Activite> {
     
    return this.http.patch<Activite>(`${this.API_URL}/activites/${id}/designation`, dto);
  }
  updateActiviteDescription(id: number, dto: TexteDto): Observable<Activite> {
     
    return this.http.patch<Activite>(`${this.API_URL}/activites/${id}/description`, dto);
  }
  updateActiviteDateDebut( id: number, dto: DateDto): Observable<Activite> {
     
    return this.http.patch<Activite>(`${this.API_URL}/activites/${id}/date-debut`, dto);
  }
    
  updateActiviteDateFin( id: number, dto: DateDto): Observable<Activite> {

    return this.http.patch<Activite>(`${this.API_URL}/activites/${id}/date-fin`, dto);
  }
}
