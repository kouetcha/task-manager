import { EmailDto, FichierInfo } from "../interfaces/base-entity-gestion";


export interface Tache {
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
  activiteId: number; // lien vers l'activité parente
  fichiers?: FichierInfo[];
  emails?: EmailDto[];
  createdAt?: Date;
  updatedAt?: Date;
}