import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environment';
import { EmailDto, MailTYPE } from '../../interfaces/base-entity-gestion';





@Injectable({
  providedIn: 'root'
})
export class EmailService {

 private readonly baseUrl= environment.API_URL + '/tasksmanager';

  constructor(private http: HttpClient) {}

  /**
   * Ajouter un email à un parent
   */
  addEmail(emailType:MailTYPE,parentId: number, email: string): Observable<EmailDto> {
    const type=this.transformeMailType(emailType);
    return this.http.post<EmailDto>(
      `${this.baseUrl}/${type}/${parentId}/emails`,
      null,
      { params: { email } }
    );
  }
transformeMailType(emailType: MailTYPE) {
  if (emailType === 'EMAIL_PROJET') return 'projets';
  if (emailType === 'EMAIL_ACTIVITE') return 'activites';
  if (emailType === 'EMAIL_TACHE') return 'taches';
  return '';
}

  /**
   * update un email
   */
  updateEmail(emailType:MailTYPE,parentId: number,emailId:number, email: string): Observable<EmailDto> {
    const type=this.transformeMailType(emailType);
    return this.http.patch<EmailDto>(
      `${this.baseUrl}/${type}/${parentId}/emails/${emailId}`,
      null,
      { params: { email } }
    );
  }

  /**
   * Supprimer un email
   */
  removeEmail(emailType:MailTYPE,parentId: number, emailId: number): Observable<void> {
     const type=this.transformeMailType(emailType);
    return this.http.delete<void>(
      `${this.baseUrl}/${type}/${parentId}/emails/${emailId}`
    );
  }

  /**
   * Activer un email
   */
  activateEmail(emailType:MailTYPE,parentId: number, emailId: number): Observable<EmailDto> {
     const type=this.transformeMailType(emailType);
    return this.http.put<EmailDto>(
      `${this.baseUrl}/${type}/${parentId}/emails/${emailId}/activate`,
      {}
    );
  }

  /**
   * Désactiver un email
   */
  deactivateEmail(emailType:MailTYPE,parentId: number, emailId: number): Observable<EmailDto> {
     const type=this.transformeMailType(emailType);
    return this.http.put<EmailDto>(
      `${this.baseUrl}/${type}/${parentId}/emails/${emailId}/deactivate`,
      {}
    );
  }

  /**
   * Récupérer tous les emails d’un parent
   */
  getEmailsByparent(emailType:MailTYPE,parentId: number): Observable<EmailDto[]> {
     const type=this.transformeMailType(emailType);
    return this.http.get<EmailDto[]>(
      `${this.baseUrl}/${type}/${parentId}/emails`
    );
  }
}