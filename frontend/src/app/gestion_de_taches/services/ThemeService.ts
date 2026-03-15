// theme.service.ts
import { Injectable, signal, effect } from '@angular/core';

export type ThemeMode = 'light' | 'dark' | 'system';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'taskflow-theme';
  private themeMode = signal<ThemeMode>('system');
  readonly currentMode = this.themeMode.asReadonly();

  constructor() {
    // Charger la préférence sauvegardée
    const saved = localStorage.getItem(this.STORAGE_KEY) as ThemeMode | null;
    if (saved && ['light', 'dark', 'system'].includes(saved)) {
      this.themeMode.set(saved);
    }
    this.applyTheme();
  }

  setTheme(mode: ThemeMode) {
    this.themeMode.set(mode);
    localStorage.setItem(this.STORAGE_KEY, mode);
    this.applyTheme();
  }

  private applyTheme() {
    const mode = this.themeMode();
    const html = document.documentElement;
    if (mode === 'system') {
      const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      html.classList.toggle('dark', systemDark);
    } else {
      html.classList.toggle('dark', mode === 'dark');
    }
  }
}