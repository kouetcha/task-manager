import { User } from "./user";

export interface Notification {
  id: number;
  message: string;
  type: NotificationType;
  event: NotificationEventType;
  seen: boolean;
  parentId?: number;

  //emetteur?: User;
  //receveur?: User;

  date: Date; // ISO date (Date côté backend spring boot)
}
export enum NotificationType {
  PROJET = 'PROJET',
  ACTIVITE='ACTIVITE',
  TACHE='TACHE'
}

export enum NotificationEventType {
  PROJET_ASSIGNE = 'PROJET_ASSIGNE',
  PROJET_MODIFIE = 'PROJET_MODIFIE',
  ACTIVITE_ASSIGNEE='ACTIVITE_ASSIGNEE',
  ACTIVITE_MODIFIEE='ACTIVITE_MODIFIEE',
  TACHE_ASSIGNEE='TACHE_ASSIGNEE',
  TACHE_MODIFIEE='TACHE_MODIFIEE'
}