// src/app/gestion_de_taches/services/projet.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environment';
import { CreateProjetDto, DateDto, Projet, TexteDto, UpdateProjetDto } from '../interfaces/base-entity-gestion';
import { Page, ProjetDto } from '../interfaces/generals';

@Injectable({
  providedIn: 'root'
})
export class ProjetService {
  private readonly API_URL = environment.API_URL + '/tasksmanager/projets';

  constructor(
    private http: HttpClient
  ) {}


  getMesProjets(
  email: string,
  page: number = 0,
  size: number = 10,
  sort: string = 'dateCreation,desc'
): Observable<Page<Projet>> {
  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sort', sort);

  return this.http.get<Page<Projet>>(
    `${this.API_URL}/email/${email}`,
    { params }
  );
}

getListProjetDto(email:string):Observable<ProjetDto[]>{
   return this.http.get<ProjetDto[]>(
    `${this.API_URL}/email/${email}/dto-list`
  );
}

 

  getProjetById(id: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.API_URL}/${id}`);
  }

  /**
   * Crée un projet avec éventuellement des fichiers.
   * Les dates sont envoyées au format ISO 8601 (ex: 2026-02-25T00:00:00.000Z)
   * Les fichiers sont structurés pour correspondre à List<FichierDTO> : fichiers[0].nomFichier, fichiers[0].fichier, etc.
   */
  createProjet(dto: CreateProjetDto, fichiers?: File[]): Observable<Projet> {
    const formData = new FormData();

    // Champs simples du DTO
    formData.append('designation', dto.designation);
    if (dto.description) formData.append('description', dto.description);

    // Dates au format ISO (Spring peut les parser par défaut)
    formData.append('dateDebut', this.formatDateISO(dto.dateDebut));
    formData.append('dateFin', this.formatDateISO(dto.dateFin));


    formData.append('status', dto.status || 'EN_ATTENTE');
    formData.append('createurId', dto.createurId.toString());

    if (dto.parentId) {
      formData.append('parentId', dto.parentId.toString());
    }

    // Emails : on les envoie sous forme de JSON (sinon Spring peut les attendre en tant que liste indexée)
    if (dto.emails && dto.emails.length > 0) {
      //formData.append('emails', JSON.stringify(dto.emails));
      dto.emails.forEach(e => formData.append("emails", e));
    }

    // Fichiers : on construit une liste d'objets FichierDTO
    if (fichiers && fichiers.length > 0) {
      fichiers.forEach((file, index) => {
        // Pour que Spring lie correctement à List<FichierDTO>

        formData.append(`fichiers[${index}].nomFichier`, file.name);
        formData.append(`fichiers[${index}].fichier`, file);
      });
    }

    return this.http.post<Projet>(this.API_URL, formData);
  }

  /**
   * Met à jour un projet (sans fichiers, en JSON)
   */
  updateProjet(id: number, dto: UpdateProjetDto): Observable<Projet> {
     
    return this.http.put<Projet>(`${this.API_URL}/${id}`, dto);
  }
  updateProjetDesignation(id: number, dto: TexteDto): Observable<Projet> {
     
    return this.http.patch<Projet>(`${this.API_URL}/${id}/designation`, dto);
  }
  updateProjetDescription(id: number, dto: TexteDto): Observable<Projet> {
     
    return this.http.patch<Projet>(`${this.API_URL}/${id}/description`, dto);
  }
  updateProjetDateDebut(id: number, dto: DateDto): Observable<Projet> {
     
    return this.http.patch<Projet>(`${this.API_URL}/${id}/date-debut`, dto);
  }
    
  updateProjetDateFin(id: number, dto: DateDto): Observable<Projet> {

    return this.http.patch<Projet>(`${this.API_URL}/${id}/date-fin`, dto);
  }

  deleteProjet(id: number): Observable<string> {
    return this.http.delete<string>(`${this.API_URL}/${id}`);
  }

  addFichier(projetId: number, file: File, nom?: string): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    if (nom) formData.append('nom', nom);
    return this.http.post<string>(`${this.API_URL}/${projetId}/fichiers`, formData);
  }

  deleteFichier(projetId: number, fichierId: number): Observable<string> {
    return this.http.delete<string>(`${this.API_URL}/${projetId}/fichiers/${fichierId}`);
  }

  private formatDateISO(date: Date | string | undefined): string {
    if (!date) return new Date().toISOString();
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toISOString();
  }
}