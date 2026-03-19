import { Observable, of, tap } from "rxjs";
import { Commentaire, CreateCommentaireDto, ELEMENTTYPE, updateCommentaireDto } from "../interfaces/base-entity-gestion";
import { Injectable } from "@angular/core";

import { HttpClient } from "@angular/common/http";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: 'root' })
export class CommentairesService {
  private readonly API_URL = environment.API_URL + '/tasksmanager/commentaires-';
  private cache = new Map<string, Commentaire[]>();

  constructor(private http: HttpClient) {}

  getByParent(elementType: ELEMENTTYPE, parentId: number): Observable<Commentaire[]> {
    const key = `${elementType}-${parentId}`;
    if (this.cache.has(key)) {
      return of(this.cache.get(key)!); // retour immédiat
    }
    return this.http.get<Commentaire[]>(`${this.API_URL}${this.transformeElementType(elementType)}/${parentId}`).pipe(
      tap(data => this.cache.set(key, data))
    );
  }

  create(elementType: ELEMENTTYPE, dto: CreateCommentaireDto, fichiers?: File[]): Observable<Commentaire> {
    const type = this.transformeElementType(elementType);
    const formData = this.buildFormData(dto, fichiers);
    return this.http.post<Commentaire>(`${this.API_URL}${type}`, formData).pipe(
      tap(() => this.invalidateCache(elementType, dto.parentId))
    );
  }

  delete(elementType: ELEMENTTYPE, id: number, parentId: number): Observable<string> {
    const type = this.transformeElementType(elementType);
    return this.http.delete<string>(`${this.API_URL}${type}/${id}`).pipe(
      tap(() => this.invalidateCache(elementType, parentId))
    );
  }

  changeContenu(elementType: ELEMENTTYPE, dto: updateCommentaireDto): Observable<Commentaire> {
    const type = this.transformeElementType(elementType);
    return this.http.patch<Commentaire>(`${this.API_URL}${type}`, dto);
  }
getCached(elementType: ELEMENTTYPE, parentId: number): Commentaire[] | null {
  return this.cache.get(`${elementType}-${parentId}`) ?? null;
}
  invalidateCache(elementType: ELEMENTTYPE, parentId: number): void {
    this.cache.delete(`${elementType}-${parentId}`);
  }

  
  
   private buildFormData(dto: CreateCommentaireDto, fichiers?: File[]): FormData {
       const formData = new FormData();
   
       formData.append('contenu', dto.contenu);
       formData.append('parentId', dto.parentId+'');
       formData.append('auteurId', dto.auteurId+'');
   
   
       if (fichiers && fichiers.length > 0) {
         fichiers.forEach((file, index) => {
           formData.append(`fichiers[${index}].nomFichier`, file.name);
           formData.append(`fichiers[${index}].fichier`, file);
         });
       }
   
       return formData;
     }


  transformeElementType(elemntType: ELEMENTTYPE) {
    if (elemntType === 'PROJET') return 'projet';
    if (elemntType === 'ACTIVITE') return 'activite';
    if (elemntType === 'TACHE') return 'tache';
    return '';
  }
   
  
}
