import { Component, EventEmitter, HostListener, Input, Output, ViewChild, ElementRef, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pdf-modal',
  imports: [CommonModule],
  templateUrl: './pdf-modal.html',
  styleUrl: './pdf-modal.css',
})
export class PdfModal implements AfterViewInit {
  @ViewChild('pdfIframe') pdfIframe!: ElementRef;
  @ViewChild('modalContainer') modalContainer!: ElementRef;

  private _pdfUrl: SafeResourceUrl|string  = "";
  @Input() fileName: string = '';
  @Input() fileSize: string = '';
  @Output() closed = new EventEmitter<void>();
  
  // Propriétés d'état
  isLoading = true;
  loadProgress = 0;
  currentPage = 1;
  pageCount: number | null = null;
  zoomLevel = 100;
  isFullscreen = false;
  
  // Propriétés privées pour la gestion du modal
  private _isOpen = false;
  private _isClosing = false;
  ngDoCheck() {
 // console.log('🔄 UploadZone change detection');
}

  constructor(
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef  
  ) {}
  
  // Désactiver la détection automatique de changement
  ngAfterViewInit() {
    this.cdr.detach(); // ⭐ Détacher le ChangeDetector
  }

  // Méthode pour mettre à jour la vue manuellement
  private updateView() {
   this.cdr.reattach();
    this.cdr.detectChanges();
    this.cdr.detach();
  }

  @Input() set pdfUrl(value: SafeResourceUrl|string) {
    //console.log('📨 pdfUrl setter called with:', value);
    
    // Ne mettre à jour que si l'URL a vraiment changé
    if (value !== this._pdfUrl) {
      this._pdfUrl = value;
      this.isLoading = true; // Recharger seulement si URL change
      //console.log('🔄 pdfUrl changed, setting loading to true');
      this.updateView(); // ⭐ Mettre à jour la vue
    } else {
      //console.log('⚠️ pdfUrl unchanged, skipping reload');
    }
  }
  
  get pdfUrl(): SafeResourceUrl|string {
    return this._pdfUrl;
  }

  // Getter/Setter pour isOpen avec logique de prévention
  @Input() set isOpen(value: boolean) {
    if (value !== this._isOpen) {
      console.log('🔄 isOpen changing from', this._isOpen, 'to', value);
      this._isOpen = value;
      
      // Mettre à jour la vue
      this.updateView();
      
      if (value) {
        // Attendre un tick pour laisser Angular mettre à jour le DOM
        setTimeout(() => {
          document.body.style.overflow = 'hidden';
          this.isLoading = true;
        }, 0);
      } else {
        setTimeout(() => {
          document.body.style.overflow = 'auto';
        }, 10);
      }
    }
  }
  
  get isOpen(): boolean {
    return this._isOpen;
  }

  // Fermer le modal
  close() {
    console.log('🛑 PdfModal.close() called');
    
    if (this._isClosing || !this._isOpen) {
      console.log('❌ Close aborted: already closing or not open');
      return;
    }
    
    console.log('✅ Proceeding with close');
    this._isClosing = true;
    this._isOpen = false;
    
    // Mettre à jour la vue
    this.updateView();
    
    // Petit délai avant d'émettre l'événement
    setTimeout(() => {
      console.log('📤 Emitting closed event');
      this.closed.emit();
      this._isClosing = false;
      console.log('✅ Close completed');
    }, 10);
  }

  // Sécurité pour l'URL
  getSafeUrl(url: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  // Gestion du clic sur le backdrop
  onBackdropClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('pdf-modal-backdrop')) {
      this.close();
      event.stopPropagation();
    }
  }

  // Chargement de l'iframe
  onIframeLoad() {
    console.log('✅ Iframe loaded');
    this.isLoading = false;
    this.loadProgress = 100;
    
    // Mettre à jour la vue
    this.updateView();
    
    // Vérifier si pdfIframe est défini
    if (!this.pdfIframe) {
      console.log('pdfIframe is not defined yet');
      return;
    }
    
    // Essayer de récupérer le nombre de pages
    try {
      const iframe = this.pdfIframe.nativeElement;
      if (iframe && (iframe.contentDocument || iframe.contentWindow)) {
        // Logique pour détecter les pages (dépend du viewer PDF)
        this.detectPageCount();
      }
    } catch (error) {
      console.log('Could not detect page count:', error);
    }
  }
  
  // Fonctionnalités supplémentaires
  openInNewTab() {
    if (this.pdfUrl) {
      window.open(this.pdfUrl.toString(), '_blank', 'noopener,noreferrer');
    }
  }

  printPdf() {
    if (this.pdfUrl) {
      const printWindow = window.open(this.pdfUrl.toString(), '_blank');
      if (printWindow) {
        printWindow.onload = () => {
          printWindow.print();
        };
      }
    }
  }

  retryLoad() {
    this.isLoading = true;
    this.loadProgress = 0;
    // Mettre à jour la vue
    this.updateView();
    
    // Simuler un rechargement
    setTimeout(() => {
      this.loadProgress = 100;
      this.isLoading = false;
      this.updateView();
    }, 1000);
  }

  zoomIn() {
    this.zoomLevel = Math.min(this.zoomLevel + 25, 400);
    this.applyZoom();
    this.updateView();
  }

  zoomOut() {
    this.zoomLevel = Math.max(this.zoomLevel - 25, 25);
    this.applyZoom();
    this.updateView();
  }

  private applyZoom() {
    const iframe = this.pdfIframe?.nativeElement;
    if (iframe) {
      iframe.style.transform = `scale(${this.zoomLevel / 100})`;
      iframe.style.transformOrigin = 'top left';
    }
  }

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    this.updateView();
    
    // Utiliser la référence ViewChild au lieu de querySelector
    const container = this.modalContainer?.nativeElement;
    if (container) {
      if (this.isFullscreen) {
        container.requestFullscreen?.();
      } else {
        document.exitFullscreen?.();
      }
    }
  }

  prevPage() {
    if (this.pageCount && this.currentPage > 1) {
      this.currentPage--;
      this.scrollToPage();
      this.updateView();
    }
  }

  nextPage() {
    if (this.pageCount && this.currentPage < this.pageCount) {
      this.currentPage++;
      this.scrollToPage();
      this.updateView();
    }
  }

  goToPage(event: Event) {
    const input = event.target as HTMLInputElement;
    const page = parseInt(input.value, 10);
    if (this.pageCount && page >= 1 && page <= this.pageCount) {
      this.currentPage = page;
      this.scrollToPage();
      this.updateView();
    }
  }

  rotatePdf() {
    // Logique de rotation
    console.log('Rotate PDF');
  }

  private scrollToPage() {
    // Implémenter le défilement vers la page
    console.log(`Go to page ${this.currentPage}`);
  }

  private detectPageCount() {
    // Détecter le nombre de pages (nécessite un viewer PDF qui expose cette info)
    this.pageCount = 1; // Exemple - à remplacer par la vraie détection
    this.updateView();
  }

  // Gestion des touches
  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent) {
    // Utiliser this._isOpen au lieu de this.isOpen pour éviter le getter
    if (!this._isOpen || this._isClosing) return;

    switch (event.key) {
      case 'Escape':
        this.close();
        event.preventDefault();
        break;
      case 'ArrowLeft':
        this.prevPage();
        break;
      case 'ArrowRight':
        this.nextPage();
        break;
      case '+':
      case '=':
        if (event.ctrlKey) {
          this.zoomIn();
          event.preventDefault();
        }
        break;
      case '-':
        if (event.ctrlKey) {
          this.zoomOut();
          event.preventDefault();
        }
        break;
      case '0':
        if (event.ctrlKey) {
          this.zoomLevel = 100;
          this.applyZoom();
          event.preventDefault();
        }
        break;
      case 'F11':
        this.toggleFullscreen();
        event.preventDefault();
        break;
    }
  }


  @HostListener('document:fullscreenchange')
  @HostListener('document:webkitfullscreenchange')
  @HostListener('document:mozfullscreenchange')
  @HostListener('document:MSFullscreenChange')
  onFullscreenChange() {
    this.isFullscreen = !!document.fullscreenElement;
    this.updateView();
  }
}