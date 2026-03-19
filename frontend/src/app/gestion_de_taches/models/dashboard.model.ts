// dashboard.model.ts
export interface DashboardStats {
  totalProjets: number;
  projetsEnCours: number;
  projetsTermines: number;
  projetsEnAttente: number;
  projetsAnnules: number;

  totalActivites: number;
  activitesEnCours: number;
  activitesTerminees: number;
  activitesEnAttente: number;
  activitesAnnulees: number;

  totalTaches: number;
  tachesEnCours: number;
  tachesTerminees: number;
  tachesEnAttente: number;
  tachesAnnulees: number;
}

export type ItemType = 'PROJET' | 'ACTIVITE' | 'TACHE';

export interface DashboardItem {
  id: number;
  designation: string;
  status: string;
  dateFin?: string;
  dateModification?: string;
  type: ItemType;
  projetId?: number;
  activiteId?: number;
}

export interface DashboardDto {
  stats: DashboardStats;
  enRetard: DashboardItem[];
  activiteRecente: DashboardItem[];
}