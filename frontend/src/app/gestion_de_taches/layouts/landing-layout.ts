import { Component } from '@angular/core';
import { RouterLink, RouterModule, RouterOutlet } from '@angular/router';
import { Landing } from '../pages/landing/landing';
;

@Component({
  selector: 'app-landing-layout',
  imports: [RouterOutlet,RouterLink,RouterModule],
  templateUrl: './landing-layout.html',
  styleUrl: './landing-layout.css',
})
export class LandingLayout {
menuOpen = false;

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }
 
  closeMenu(): void {
    this.menuOpen = false;
  }
  currentYear: number = new Date().getFullYear();
}
