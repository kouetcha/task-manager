export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;      
  size: number;
  first: boolean;
  last: boolean;
}

export interface BaseEntityDto {
  id: number;
  designation: string;
  description?: string;
  dateDebut: Date | string;
  dateFin: Date | string;
  status: Status;
  createurId: number;
  createurNom: string;
  createurPrenom: string;
  createurEmail: string;
  parentId?: number;
}

export enum Status {
  EN_ATTENTE = 'EN_ATTENTE',
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  EN_PAUSE = 'EN_PAUSE',
  ANNULLE = 'ANNULLE'
}

export interface ProjetDto extends BaseEntityDto {
  parentId?: never; // Projet n'a pas de parent
}

export interface ActiviteDto extends BaseEntityDto {
  parentId: number; // projetId — obligatoire
}

export interface TacheDto extends BaseEntityDto {
  parentId: number; // activiteId — obligatoire
}
type PageProjet   = Page<ProjetDto>;
type PageActivite = Page<ActiviteDto>;
type PageTache    = Page<TacheDto>;