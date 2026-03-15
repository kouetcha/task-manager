import { Component } from '@angular/core';
import { Sidebar } from './sidebar';
import { RouterOutlet } from '@angular/router';
import { Header } from './header';

@Component({
  selector: 'app-auth-layout',
  imports: [Sidebar,RouterOutlet,Header],
  templateUrl: './auth-layout.html',
  styleUrl: './auth-layout.css',
})
export class AuthLayout {

}
