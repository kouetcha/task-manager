import { UserCategory } from "../models/user";

export interface CreateUserDto {
  email: string;
  telephone: string;
  nom: string;
  prenom: string;
  category: UserCategory;
  motdepasse: string;
  roleProjet:string;
}

export interface UpdateUserDto {
  email?: string;
  telephone?: string;
  nom?: string;
  category: UserCategory;
  prenom?: string;
}

export interface ChangePasswordDto {
  email: string;
  ancienmotdepasse: string;
  motdepasse: string;
}

export interface LoginDto {
  email: string;
  motdepasse: string;
}
export interface User {
  id: number;
  dateCreation: string;
  dateModification: string;
  email: string;
  telephone: string;
  dernierConnexion: string | null;
  nom: string;
  prenom: string;
  admin: boolean;
  category: 'NORMAL' | 'ADMIN' | 'SUPER_ADMIN';
  est_actif: boolean;
}
