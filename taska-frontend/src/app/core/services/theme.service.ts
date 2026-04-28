import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'taska-theme';
  isDark = signal<boolean>(false);

  constructor() {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const dark = saved === 'dark' || (saved === null && prefersDark);
    this.isDark.set(dark);
    this.applyTheme(dark);
  }

  toggle(): void {
    const next = !this.isDark();
    this.isDark.set(next);
    this.applyTheme(next);
    localStorage.setItem(this.STORAGE_KEY, next ? 'dark' : 'light');
  }

  private applyTheme(dark: boolean): void {
    document.documentElement.classList.toggle('dark', dark);
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
  }
}
