import { Directive, ElementRef, OnInit, OnDestroy } from '@angular/core';

@Directive({
  selector: '[scrollContain]',
  standalone: true
})
export class ScrollContainDirective implements OnInit, OnDestroy {

  private el: HTMLElement;

  constructor(private elementRef: ElementRef) {
    this.el = this.elementRef.nativeElement;
  }

  ngOnInit() {
    this.el.addEventListener('touchstart', this.onTouchStart, { passive: false });
    this.el.addEventListener('touchmove', this.onTouchMove, { passive: false });
  }

  ngOnDestroy() {
    this.el.removeEventListener('touchstart', this.onTouchStart);
    this.el.removeEventListener('touchmove', this.onTouchMove);
  }

  private startY = 0;

  private onTouchStart = (e: TouchEvent) => {
    this.startY = e.touches[0].clientY;
  };

  private onTouchMove = (e: TouchEvent) => {
    const el = this.el;
    const deltaY = e.touches[0].clientY - this.startY;
    const scrollTop = el.scrollTop;
    const scrollHeight = el.scrollHeight;
    const clientHeight = el.clientHeight;

    const atTop = scrollTop === 0 && deltaY > 0;
    const atBottom = scrollTop + clientHeight >= scrollHeight && deltaY < 0;

    // Bloque la propagation UNIQUEMENT quand on est au bord
    if (atTop || atBottom) {
      e.preventDefault();
    }
  };
}