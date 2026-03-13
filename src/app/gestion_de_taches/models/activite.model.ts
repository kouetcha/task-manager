import { EmailDto, FichierInfo } from "../interfaces/base-entity-gestion";
import { Tache } from "./tache.model";

export interface Activite {
  id: number;
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  createurId: number;
  createur?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  projetId: number; // lien vers le projet parent
  fichiers?: FichierInfo[];
  emails?: EmailDto[];
  taches?: Tache[]; // optionnel, si on veut les charger avec l'activité
  createdAt?: Date;
  updatedAt?: Date;
}