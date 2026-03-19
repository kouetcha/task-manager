// dashboard.component.ts
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';


import { AuthService } from '../../services/AuthService';
import { User } from '../../models/user';
import { DashboardService } from '../../services/dashboard.service';
import { MaterialModule } from '../../material.module';
import { DashboardItem, DashboardStats } from '../../models/dashboard.model';
import { ScrollContainDirective } from '../../directives/scroll-contain.directive';



@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  imports:[MaterialModule, ScrollContainDirective]
})
export class Dashboard implements OnInit {

  loading = true;
  user:User|null=null;



  stats: DashboardStats = {
    totalProjets: 0, projetsEnCours: 0, projetsTermines: 0,
    projetsEnAttente: 0, projetsAnnules: 0,
    totalActivites: 0, activitesEnCours: 0, activitesTerminees: 0,
    activitesEnAttente: 0, activitesAnnulees: 0,
    totalTaches: 0, tachesEnCours: 0, tachesTerminees: 0,
    tachesEnAttente: 0, tachesAnnulees: 0,
  };

  enRetard: DashboardItem[] = [];
  activiteRecente: DashboardItem[] = [];

  tachesBars = [
    { label: 'Terminées',  color: 'bg-green-400',  count: 0, key: 'tachesTerminees' },
    { label: 'En cours',   color: 'bg-blue-400',   count: 0, key: 'tachesEnCours' },
    { label: 'En attente', color: 'bg-yellow-400', count: 0, key: 'tachesEnAttente' },
    { label: 'Annulées',   color: 'bg-red-400',    count: 0, key: 'tachesAnnulees' },
  ];

  projetsBars = [
    { label: 'Terminés',   color: 'bg-green-400',  count: 0, key: 'projetsTermines' },
    { label: 'En cours',   color: 'bg-blue-400',   count: 0, key: 'projetsEnCours' },
    { label: 'En attente', color: 'bg-yellow-400', count: 0, key: 'projetsEnAttente' },
    { label: 'Annulés',    color: 'bg-red-400',    count: 0, key: 'projetsAnnules' },
  ];

  constructor(
    private router: Router,
    private dashboardService: DashboardService, 
    private authService: AuthService,  
    private cdr: ChangeDetectorRef        
  ) {
    this.authService.user$.subscribe(user=>this.user=user);
  }

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    this.loading = true;
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.stats = data.stats;
        this.enRetard = data.enRetard;
        this.activiteRecente = data.activiteRecente;
       
        this.tachesBars.forEach(b => b.count = (this.stats as any)[b.key]);
        this.projetsBars.forEach(b => b.count = (this.stats as any)[b.key]);
        this.loading = false;
        this.cdr.detectChanges()
      },
      error: () => this.loading = false
    });
  }

  getPercent(value: number, total: number): number {
    if (!total) return 0;
    return Math.round((value / total) * 100);
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      EN_COURS: 'En cours', TERMINE: 'Terminé',
      EN_ATTENTE: 'En attente', ANNULE: 'Annulé'
    };
    return map[status] ?? status;
  }
navigateToEnRetard() {
  // Redirige vers la page tâches filtrée sur "en retard"
  this.router.navigate(['/taches'], { queryParams: { filter: 'retard' } });
}

navigateToRecent() {
  this.router.navigate(['/projets']);
}
  getStatusColor(status: string): string {
    const map: Record<string, string> = {
      EN_COURS:   'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300',
      TERMINE:    'bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-300',
      EN_ATTENTE: 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-700 dark:text-yellow-300',
      ANNULE:     'bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-300',
    };
    return map[status] ?? '';
  }

  navigateTo(item: DashboardItem) {
    if (item.type === 'PROJET') {
      this.router.navigate(['/app/projets', item.id]);
    } else if (item.type === 'ACTIVITE') {
      this.router.navigate(['/app/activites', item.id]);
    } else {
      this.router.navigate(['/app/taches', item.id]);
    }
  }
}