import { Activite } from "../models/activite.model";
import { Tache } from "../models/tache.model";
import { User } from "../models/user";

export interface BaseEntityGestionDto {
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status:'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  createurId: number;
  emails?: string[];
  parentId?: number;
  fichiers?: [FichierInfo];
}

export class Projet {

  id!: number;
  designation!: string;
  description?: string;
  dateDebut!: Date;
  dateFin!: Date;
  status!: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  createurId!: number;
  emailType:MailTYPE="EMAIL_PROJET";
  createur?: CreateurDto;
  

  fichiers?: FichierInfo[];
  dateUpload?: Date;
  createdAt?: Date;
  updatedAt?: Date;
  emails?: EmailDto[];

  get simpleEmails(): string {
    return this.emails
      ?.map(e => e.email)
      .join(',') ?? '';
  }
}
export interface  CreateurDto {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  
export class Entite {

  id!: number;
  designation!: string;
  description?: string;
  dateDebut!: Date;
  dateFin!: Date;
  status!: 'EN_ATTENTE' | 'EN_COURS' | 'TERMINE' | 'ANNULE';
  createurId!: number;
  createur?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };

  fichiers?: FichierInfo[];
  dateUpload?: Date;
  createdAt?: Date;
  updatedAt?: Date;
  emails?: EmailDto[];

  get simpleEmails(): string {
    return this.emails
      ?.map(e => e.email)
      .join(',') ?? '';
  }
}
export type MailTYPE = 'EMAIL_PROJET' | 'EMAIL_ACTIVITE' | 'EMAIL_TACHE';
export type ELEMENTTYPE = 'PROJET' | 'ACTIVITE' | 'TACHE';
export type EDITABLETYPE='DESIGNATION' | 'DESCRIPTION' |'DATE_DEBUT' |'DATE_FIN';
export interface TexteDto {
  texte?: string;
}
export interface DateDto {
  date: Date;
}
export interface EditableDto{
  type:EDITABLETYPE;
  texte?:string;
  date?:Date
  projet:Projet;
}
export interface EditableActivieDto{
  type:EDITABLETYPE;
  texte?:string;
  date?:Date
  activite:Activite;
}
export interface EditableTacheDto{
  type:EDITABLETYPE;
  texte?:string;
  date?:Date
  tache:Tache;
}
export interface EmailDto{
  id:number;
  email:string;
  entiteId:number;
  active: boolean;
}

export interface FichierInfo {
  id: number;
  nomFichier: string;
  cheminFichier?: string;
  dateUpload?: Date;
  type:string;
  url:string;
  callbackurl:string;
}

export interface CreateProjetDto {
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status?: string;
  createurId: number;
  emails?: string[];
  parentId?: number;
}
export interface CreateCommentaireDto {
  contenu: string;
  auteurId: number;
  parentId: number;
}

export interface Commentaire {
  id:number
  contenu: string;
  date: Date;
  fichiers?: FichierInfo[];
  auteur:User;

 
}
export interface updateCommentaireDto {
  contenu: string;
  id: number;
}

export interface UpdateProjetDto {
  designation?: string;
  description?: string;
  dateDebut?: Date;
  createurId: number;
  dateFin?: Date;
  status?: string;
  emails?: string[];
}


export interface CreateEntiteDto {
  designation: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  status?: string;
  createurId?: number;
  emails?: string[];
  parentId?: number;
}

export interface UpdateEntiteDto {
  designation?: string;
  description?: string;
  dateDebut?: Date;
  createurId?: number;
  dateFin?: Date;
  status?: string;
  emails?: string[];
}
export interface CreateActiviteDto extends CreateEntiteDto{

}
export interface CreateTacheDto extends CreateEntiteDto{
  id?:number;

}
export interface UpdateActiviteDto extends UpdateEntiteDto{
 id?:number;
}
export interface UpdateTacheDto extends UpdateEntiteDto{
  
}

