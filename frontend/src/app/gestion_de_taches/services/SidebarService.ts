// sidebar.service.ts
import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  private _isOpen = signal(false);
  isOpen = this._isOpen.asReadonly();

  private _isDesktop = signal(false);
  isDesktop = this._isDesktop.asReadonly();

  constructor() {
    if (typeof window !== 'undefined') {
      this.checkScreenSize();
      window.addEventListener('resize', () => this.checkScreenSize());
    }
  }

  private checkScreenSize() {
    const desktop = window.innerWidth >= 1024; // breakpoint lg
    this._isDesktop.set(desktop);
    
    // Optionnel : ouvrir par défaut sur desktop (si vous le souhaitez)
    // if (desktop && !this._isOpen()) {
    //   this._isOpen.set(true);
    // }
  }

  toggle() {
    this._isOpen.update(v => !v);
  }

  open() {
    this._isOpen.set(true);
  }

  close() {
    this._isOpen.set(false);
  }
}