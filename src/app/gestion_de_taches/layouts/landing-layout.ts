import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Landing } from '../pages/landing/landing';

@Component({
  selector: 'app-landing-layout',
  imports: [RouterOutlet,RouterLink,Landing],
  templateUrl: './landing-layout.html',
  styleUrl: './landing-layout.css',
})
export class LandingLayout {

}
