
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardDto, MenuStats } from '../models/dashboard.model';


@Injectable({ providedIn: 'root' })
export class DashboardService {

  private readonly url = `${environment.API_URL}/tasksmanager/statistiques`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardDto> {
    return this.http.get<DashboardDto>(this.url+"/dashboard").pipe(
      catchError(() => of(this.emptyDashboard()))
    );
  }
    getMenuStats(): Observable<MenuStats> {
    
    return this.http.get<MenuStats>(this.url+"/menu").pipe(
      catchError(() => of(this.emptyMenu()))
    );
  }
  private emptyMenu():MenuStats{
    return {
      nbreProjets:0,
      nbrActivites:0,
      nbrTaches:0
    }
  }

  private emptyDashboard(): DashboardDto {
    return {
      stats: {
        totalProjets: 0, projetsEnCours: 0, projetsTermines: 0,
        projetsEnAttente: 0, projetsAnnules: 0,
        totalActivites: 0, activitesEnCours: 0, activitesTerminees: 0,
        activitesEnAttente: 0, activitesAnnulees: 0,
        totalTaches: 0, tachesEnCours: 0, tachesTerminees: 0,
        tachesEnAttente: 0, tachesAnnulees: 0,
      },
      enRetard: [],
      activiteRecente: [],
    };
  }
}