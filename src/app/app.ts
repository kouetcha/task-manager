import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CustomNotification } from './gestion_de_taches/components/notifications/custom-notification/custom-notification';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,CustomNotification],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('gestion_de_taches_frontend');
}
