import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { ELEMENTTYPE, Entite, FichierInfo, MailTYPE } from '../interfaces/base-entity-gestion';
import { environment } from '../../../environments/environment';



export interface FichierEntityGestion {
  id: number;
  nomFichier: string;
  fileCode: string;
  cheminFichier:string;
  type?: string;
  taille?: number;
  dateUpload?: Date;
  projet?:Entite;
  activite?:Entite;
  tache?:Entite;
  url?:string;
}

@Injectable({
  providedIn: 'root',
})
export class FichierService {
  private readonly baseUrl = environment.API_URL + '/tasksmanager/fichiers-';

  constructor(private http: HttpClient) {}

  /**
   * Upload un ou plusieurs fichiers pour un parent
   * @param parentId ID du parent
   * @param files Liste des fichiers à uploader
   * @param nomsFichiers (optionnel) Liste des noms personnalisés pour les fichiers (même ordre)
   */
  uploadFiles(elementType:ELEMENTTYPE,parentId: number, files: File[], nomsFichiers?: string[]): Observable<any> {
    const type=this.transformeElementType(elementType);
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));

    let params = new HttpParams();
    if (nomsFichiers && nomsFichiers.length) {
      nomsFichiers.forEach(nom => params = params.append('nomsFichiers', nom));
    }

    return this.http.post(`${this.baseUrl}${type}/${parentId}/upload`, formData, {
      params: params,
      headers: new HttpHeaders({
        // Ne pas définir 'Content-Type' car Angular le définit automatiquement avec FormData
      })
    });
  }

  transformeElementType(elemntType: ELEMENTTYPE) {
    if (elemntType === 'PROJET') return 'projet';
    if (elemntType === 'ACTIVITE') return 'activite';
    if (elemntType === 'TACHE') return 'tache';
    return '';
  }

  /**
   * Télécharger un fichier par son fileCode
   * @param fileCode Code unique du fichier
   * @returns Observable<Blob> contenant le fichier
   */
  downloadFile(elementType:ELEMENTTYPE,fileCode: string): Observable<Blob> {
    const type=this.transformeElementType(elementType);
    return this.http.get(`${this.baseUrl}${type}/download/${fileCode}`, {
      responseType: 'blob'
    });
  }

  /**
   * Supprimer un fichier spécifique
   * @param parentId ID du parent (inclus dans l'URL)
   * @param fichierId ID du fichier à supprimer
   */
  deleteFile(elementType:ELEMENTTYPE,parentId: number, fichierId: number): Observable<any> {
     const type=this.transformeElementType(elementType);
    return this.http.delete(`${this.baseUrl}${type}/${parentId}/${fichierId}`);
  }

  /**
   * Supprimer tous les fichiers d'un parent
   * @param parentId ID du parent
   */
  deleteAllFiles(elementType:ELEMENTTYPE,parentId: number): Observable<any> {
    const type=this.transformeElementType(elementType);
    return this.http.delete(`${this.baseUrl}${type}/${parentId}`);
  }

  /**
   * Lister tous les fichiers d'un parent
   * @param parentId ID du parent
   * @returns Liste des fichiers (FichierEntityGestion)
   */
  listFiles(elementType:ELEMENTTYPE,parentId: number): Observable<FichierInfo[]> {
     const type=this.transformeElementType(elementType);
    return this.http.get<FichierInfo[]>(`${this.baseUrl}${type}/${parentId}`);
  }
}
