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
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Dashboard },
      {path:'projects',component:Projets},
       {path:'activites',component:Activites},
      {path:'projects/:id',component:ProjetDetail},
       {path:'projets/:id',component:ProjetDetail}
     
    ],
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}