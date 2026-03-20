// websocket.service.ts
import { Injectable, inject } from '@angular/core';
import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './AuthService';

export interface NotificationEvent {
  _mid?:      string;
  type:       string;
  message:    string;
  entiteId:   number;
  entiteType: 'PROJET' | 'ACTIVITE' | 'TACHE';
  emetteur:   string;
  timestamp:  string;
}

export interface PresenceEvent {
  userId:    number;
  fullName:  string;
  online:    boolean;
  timestamp: string;
}

export interface ChatMessage {
  _mid?:      string;
  senderId:   number;
  senderName: string;
  receiverId: number | null;
  projetId:   number | null;
  content:    string;
  timestamp:  string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService {

  private authService = inject(AuthService);
  private rxStomp     = new RxStomp();

  // ── Identifiant unique de cet onglet/appareil ────────────────
  // Chaque onglet a son propre sessionId → pas de confusion entre sessions
  readonly sessionId = this.generateId();
private generateId(): string {
  // crypto.randomUUID disponible en HTTPS/localhost uniquement
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }

  // Fallback universel — fonctionne partout (HTTP, mobile, vieux navigateurs)
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0;
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}
  // ── Déduplique les messages reçus sur cet onglet ─────────────
  // Evite qu'un même message soit traité 2x si connect() est appelé 2x
  private processedIds = new Set<string>();

  // ── Connexion ─────────────────────────────────────────────────
  connect(): void {
    // ✅ Empêche les connexions multiples sur le même onglet
    if (this.rxStomp.active) {
      console.warn('[WS] Déjà connecté — connexion ignorée');
      return;
    }

    const token = this.authService.token;
    if (!token) {
      console.error('[WS] Pas de token — connexion annulée');
      return;
    }

    const brokerURL = environment.API_URL
      .replace('http://', 'ws://')
      .replace('https://', 'wss://')
      + '/tasksmanager/ws';

    this.rxStomp.configure({
      brokerURL,
      connectHeaders: {
        Authorization:  `Bearer ${token}`,
        'X-Session-Id': this.sessionId, // identifie cet onglet côté backend
      },
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
      reconnectDelay:    5000,          // reconnexion auto si coupure réseau
    });

    this.rxStomp.activate();
  }

  // ── Déconnexion ───────────────────────────────────────────────
  disconnect(): void {
    if (this.rxStomp.active) {
      this.rxStomp.deactivate();
    }
  }

  // ── Notifications personnelles ────────────────────────────────
  // Chaque appareil/onglet reçoit la notification
  // Le _mid déduplique si le même onglet la reçoit 2x
  onNotification(): Observable<NotificationEvent> {
    return this.rxStomp
      .watch('/user/queue/notifications')
      .pipe(
        map(msg => JSON.parse(msg.body) as NotificationEvent),
        filter(event => this.deduplicate(event._mid))
      );
  }

  // ── Mises à jour d'un projet ──────────────────────────────────
  onProjetUpdate(projetId: number): Observable<NotificationEvent> {
    return this.rxStomp
      .watch(`/topic/projets/${projetId}`)
      .pipe(
        map(msg => JSON.parse(msg.body) as NotificationEvent),
        filter(event => this.deduplicate(event._mid))
      );
  }

  // ── Mises à jour d'une activité ───────────────────────────────
  onActiviteUpdate(activiteId: number): Observable<NotificationEvent> {
    return this.rxStomp
      .watch(`/topic/activites/${activiteId}`)
      .pipe(
        map(msg => JSON.parse(msg.body) as NotificationEvent),
        filter(event => this.deduplicate(event._mid))
      );
  }

  // ── Mises à jour d'une tâche ──────────────────────────────────
  onTacheUpdate(tacheId: number): Observable<NotificationEvent> {
    return this.rxStomp
      .watch(`/topic/taches/${tacheId}`)
      .pipe(
        map(msg => JSON.parse(msg.body) as NotificationEvent),
        filter(event => this.deduplicate(event._mid))
      );
  }

  // ── Messages de chat ─────────────────────────────────────────
  onChatMessage(): Observable<ChatMessage> {
    return this.rxStomp
      .watch('/user/queue/messages')
      .pipe(
        map(msg => JSON.parse(msg.body) as ChatMessage),
        filter(msg => this.deduplicate(msg._mid))
      );
  }

  // ── Présence ─────────────────────────────────────────────────
  onPresence(): Observable<PresenceEvent> {
    return this.rxStomp
      .watch('/topic/presence')
      .pipe(map(msg => JSON.parse(msg.body) as PresenceEvent));
  }

  // ── Envoi message chat ────────────────────────────────────────
  sendChatMessage(message: Partial<ChatMessage>): void {
    this.rxStomp.publish({
      destination: '/app/chat.send',
      body:        JSON.stringify(message),
    });
  }

  // ── État connexion ────────────────────────────────────────────
  get connected$(): Observable<boolean> {
    return this.rxStomp.connected$.pipe(
      map(state => state === RxStompState.OPEN)
    );
  }

  // ── Déduplique par _mid ───────────────────────────────────────
  // Retourne true si le message doit être traité
  // Retourne false s'il a déjà été traité sur CET onglet
  private deduplicate(mid?: string): boolean {
    if (!mid) return true; // pas d'id → laisse passer

    if (this.processedIds.has(mid)) {
      console.warn('[WS] Message dupliqué ignoré :', mid);
      return false;
    }

    this.processedIds.add(mid);

    // Nettoie après 30s pour éviter la fuite mémoire
    setTimeout(() => this.processedIds.delete(mid), 30_000);

    return true;
  }
}