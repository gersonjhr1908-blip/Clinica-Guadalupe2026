import { Component, OnInit } from '@angular/core';

type AccessibilityMode = 'contrast' | 'invert' | 'colorblind';

@Component({
  selector: 'app-accessibility',
  standalone: true,
  templateUrl: './accessibility.component.html',
  styleUrl: './accessibility.component.css',
})
export class AccessibilityComponent implements OnInit {
  isOpen = false;
  fontScale = 100;
  activeMode: AccessibilityMode | null = null;
  isReading = false;

  ngOnInit(): void {
    const savedScale = Number(localStorage.getItem('cg-a11y-font-scale'));
    this.fontScale = savedScale === 90 || savedScale === 100 || savedScale === 115 || savedScale === 130 ? savedScale : 100;
    const savedMode = localStorage.getItem('cg-a11y-mode') as AccessibilityMode | null;
    this.activeMode = savedMode === 'contrast' || savedMode === 'invert' || savedMode === 'colorblind' ? savedMode : null;
    this.applySettings();
  }

  togglePanel(): void { this.isOpen = !this.isOpen; }

  changeFont(step: number): void {
    this.fontScale = Math.min(130, Math.max(90, this.fontScale + step));
    this.applySettings();
  }

  setMode(mode: AccessibilityMode): void {
    this.activeMode = this.activeMode === mode ? null : mode;
    this.applySettings();
  }

  readPage(): void {
    if (!('speechSynthesis' in window)) return;
    if (speechSynthesis.speaking) {
      speechSynthesis.cancel();
      this.isReading = false;
      return;
    }
    const content = document.querySelector('main, app-root')?.textContent?.replace(/\s+/g, ' ').trim() ?? '';
    if (!content) return;
    const speech = new SpeechSynthesisUtterance(content);
    speech.lang = 'es-PE';
    speech.rate = 0.95;
    speech.onend = () => this.isReading = false;
    speech.onerror = () => this.isReading = false;
    this.isReading = true;
    speechSynthesis.speak(speech);
  }

  reset(): void {
    if ('speechSynthesis' in window) speechSynthesis.cancel();
    this.fontScale = 100;
    this.activeMode = null;
    this.isReading = false;
    this.applySettings();
  }

  private applySettings(): void {
    const page = document.documentElement;
    page.style.fontSize = `${this.fontScale}%`;
    page.classList.remove('a11y-high-contrast', 'a11y-invert', 'a11y-colorblind');
    if (this.activeMode) page.classList.add(`a11y-${this.activeMode === 'contrast' ? 'high-contrast' : this.activeMode}`);
    localStorage.setItem('cg-a11y-font-scale', String(this.fontScale));
    if (this.activeMode) localStorage.setItem('cg-a11y-mode', this.activeMode);
    else localStorage.removeItem('cg-a11y-mode');
  }
}
