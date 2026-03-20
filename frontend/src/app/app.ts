import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CustomNotification } from './gestion_de_taches/components/notifications/custom-notification/custom-notification';
import { WebSocketService } from './gestion_de_taches/services/websocket.service';
import { AuthService } from './gestion_de_taches/services/AuthService';
import { filter, Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,CustomNotification],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('TaskFlow');


  

  
}
