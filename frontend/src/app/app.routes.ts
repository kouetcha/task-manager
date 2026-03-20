// app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthLayout } from './gestion_de_taches/layouts/auth-layout';


import { Register } from './gestion_de_taches/pages/register/register';

import { LandingLayout } from './gestion_de_taches/layouts/landing-layout';
import { Landing } from './gestion_de_taches/pages/landing/landing';
import { Login } from './gestion_de_taches/pages/login/login';
import { Dashboard } from './gestion_de_taches/pages/dashboard/dashboard';
import { authGuard } from './gestion_de_taches/commons/auth-guard';
import { Projets } from './gestion_de_taches/pages/projets/projets';
import { ProjectDetail } from './gestion_de_taches/pages/project-detail/project-detail';
import { ProjetDetail } from './gestion_de_taches/pages/projet-detail/projet-detail';
import { Activites } from './gestion_de_taches/pages/activites/activites';
import { Taches } from './gestion_de_taches/pages/taches/taches';
import { ActiviteDetails } from './gestion_de_taches/pages/activite-details/activite-details';
import { TacheDetails } from './gestion_de_taches/pages/tache-details/tache-details';
import { AuthGuard } from './gestion_de_taches/auth/auth.guard';
import { Calendar } from './gestion_de_taches/pages/calendar/calendar';
import { Settings } from './gestion_de_taches/pages/settings/settings';
import { Profil } from './gestion_de_taches/pages/profil/profil';
import { Notifications } from './gestion_de_taches/pages/notifications/notifications';


export const routes: Routes = [
  {
    path: '',
    component: LandingLayout,
    children: [
      { path: '', component: Landing },
      { path: 'login', component: Login },
      { path: 'register', component: Register},
    ],
  },
  {
    path: 'app',
    component: AuthLayout,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Dashboard },
      {path:'projets', component:Projets},
      {path:'projets/:id', component:ProjetDetail},
      {path:'activites', component:Activites},
      {path:'activites/:id', component:ActiviteDetails},
      {path:'taches', component:Taches},
      {path:'taches/:id', component:TacheDetails},
      {path:'calendar', component:Calendar},
      {path:'settings', component:Settings},
      {path:'profile', component:Profil},
      {path:'notifications',component:Notifications}
     
     
    ],
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}