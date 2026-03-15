// src/app/gestion_de_taches/services/tache.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Tache } from '../models/tache.model';
import { CreateTacheDto, DateDto, TexteDto, UpdateTacheDto } from '../interfaces/base-entity-gestion';
import { Page } from '../interfaces/generals';


@Injectable({
  providedIn: 'root'
})
export class TacheService {
  private readonly API_URL = environment.API_URL + '/tasksmanager'; // attention : le chemin complet est /activite/{activiteId}/taches

  constructor(private http: HttpClient) {}

  /**
   * Récupère toutes les activités d'un activite
   */
  getByActivite(activiteId: number): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.API_URL}/taches/activites/${activiteId}`);
  }
  getByActiviteAndEmail(activiteId: number,email:string): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.API_URL}/taches/activites/${activiteId}/email/${email}`);
  }
  /**
   * Récupère une activité par son ID
   */
  getById(id: number): Observable<Tache> {

    return this.http.get<Tache>(`${environment.API_URL}/tasksmanager/taches/${id}`);
  }

  /**
   * Crée une activité dans un activite
   * @param activiteId ID du activite parent
   * @param dto Données de l'activité
   * @param fichiers Fichiers optionnels
   */
  create(activiteId: number, dto: CreateTacheDto, fichiers?: File[]): Observable<Tache> {
    const formData = this.buildFormData(dto, fichiers);
    return this.http.post<Tache>(`${this.API_URL}/taches/activites/${activiteId}`, formData);
  }

  /**
   * Met à jour une activité (sans fichiers, en JSON)
   */
  update( id: number, dto: UpdateTacheDto): Observable<Tache> {
    // Le PUT s'attend à un JSON, pas de FormData
    return this.http.put<Tache>(`${this.API_URL}/taches/${id}`, dto);
  }
  getMesTaches(
  email: string,
  page: number = 0,
  size: number = 10,
  sort: string = 'dateCreation,desc'
): Observable<Page<Tache>> {
  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sort', sort);

  return this.http.get<Page<Tache>>(
    `${this.API_URL}/taches/email/${email}`,
    { params }
  );
}

  /**
   * Supprime une activité
   */
  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.API_URL}/tasksmanager/taches/${id}`);
  }

  /**
   * Ajoute un fichier à une activité existante
   */
  addFichier(tacheId: number, file: File, nom?: string): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    if (nom) formData.append('nom', nom);
    return this.http.post<string>(`${environment.API_URL}/tasksmanager/taches/${tacheId}/fichiers`, formData);
  }

  /**
   * Supprime un fichier d'une activité
   */
  deleteFichier(tacheId: number, fichierId: number): Observable<string> {
    return this.http.delete<string>(`${this .API_URL}/tasksmanager/taches/${tacheId}/fichiers/${fichierId}`);
  }

  /**
   * Construit un FormData à partir du DTO et des fichiers
   */
  private buildFormData(dto: CreateTacheDto, fichiers?: File[]): FormData {
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

    // Fichiers sous forme de liste indexée (comme dans ActiviteService)
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

  updateTacheDesignation( id: number, dto: TexteDto): Observable<Tache> {
     
    return this.http.patch<Tache>(`${this.API_URL}/taches/${id}/designation`, dto);
  }
  updateTacheDescription( id: number, dto: TexteDto): Observable<Tache> {
     
    return this.http.patch<Tache>(`${this.API_URL}/taches/${id}/description`, dto);
  }
  updateTacheDateDebut(id: number, dto: DateDto): Observable<Tache> {
     
    return this.http.patch<Tache>(`${this.API_URL}/taches/${id}/date-debut`, dto);
  }
    
  updateTacheDateFin(id: number, dto: DateDto): Observable<Tache> {

    return this.http.patch<Tache>(`${this.API_URL}/taches/${id}/date-fin`, dto);
  }
}
