import { Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, Output, EventEmitter, SimpleChanges, ViewChild } from '@angular/core';
import { EditorMode } from '../../../models/document.model';
import { OnlyOfficeService } from '../../../services/editeur-lecteur/onlyoffice-service';
import { FichierInfo } from '../../../interfaces/base-entity-gestion';
import { MatIcon } from '@angular/material/icon';
import { MatSpinner } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-onlyoffice-viewer',
  templateUrl: './onlyoffice-viewer.html',
  styleUrls: ['./onlyoffice-viewer.css'],
  imports:[MatIcon,MatSpinner,CommonModule]
})
export class OnlyOfficeViewer implements OnInit, OnChanges, OnDestroy {
  @ViewChild('editorContainer') editorContainer!: ElementRef<HTMLDivElement>;

  @Input() fileInfo!: FichierInfo;
  @Input() mode: EditorMode='edit';   // Mode d'édition par défaut
  @Input() autoSave: boolean = true;    // Sauvegarde automatique ?
  @Input() customization: any = {};     // Personnalisation supplémentaire

  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();
  @Output() error = new EventEmitter<string>();
  @Output() documentReady = new EventEmitter<void>();

  // États
  isLoading = true;
  hasError = false;
  errorMessage = '';
  isEditorLoaded = false;

  // Propriétés exposées au template pour les étapes de chargement
  get scriptLoaded(): boolean {
    return this.onlyOfficeService.isScriptLoaded();
  }

  get editorInitialized(): boolean {
    return this.onlyOfficeService.isEditorInitialized();
  }

  get isDocumentReady(): boolean {
    return this.isEditorLoaded;
  }

  constructor(private onlyOfficeService: OnlyOfficeService) {}

  ngOnInit(): void {
    this.initializeViewer();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Si l'URL ou le fichier change, on réinitialise
    if ((changes['fileUrl'] && !changes['fileUrl'].firstChange) ||
        (changes['fileName'] && !changes['fileName'].firstChange)) {
      this.destroyEditor();
      this.initializeViewer();
    }
  }

  ngOnDestroy(): void {
    this.destroyEditor();
  }

  private async initializeViewer(): Promise<void> {
    if (!this.fileInfo.url || !this.fileInfo.callbackurl) {
      console.log("FichierInfo::: ")
      console.log(this.fileInfo)
      this.handleError('URL ou nom de fichier manquant');
      return;
    }

    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';

    try {
      // 1. Vérifier la connexion au serveur ONLYOFFICE
      const isConnected = await this.onlyOfficeService.checkServerConnection();
      if (!isConnected) {
        throw new Error('Impossible de se connecter au serveur ONLYOFFICE');
      }

      // 2. Charger le script ONLYOFFICE
      await this.onlyOfficeService.loadScript();

      // 3. Créer un objet FichierInfo temporaire pour la configuration
      const fileInfo: FichierInfo = {
        id: this.fileInfo.id || 0,
        nomFichier: this.fileInfo.nomFichier,
        url: this.fileInfo.url,
        type: this.fileInfo.type,
        callbackurl:this.fileInfo.callbackurl
      };

      // 4. Construire la configuration
      const config = this.onlyOfficeService.createConfig(fileInfo,this.mode, this.customization);

      // 5. Attendre que le conteneur soit prêt
      setTimeout(() => {
        if (!this.editorContainer) {
          this.handleError('Conteneur de l\'éditeur introuvable');
          return;
        }

        // Donner un ID unique au conteneur
        const containerId = 'onlyoffice-editor-' + Date.now();
        this.editorContainer.nativeElement.id = containerId;

        // 6. Initialiser l'éditeur
        this.onlyOfficeService.initializeEditor(containerId, config)
          .then((instance) => {
            console.log('Éditeur ONLYOFFICE initialisé avec succès', instance);
            this.isLoading = false;
            this.isEditorLoaded = true;

            // S'abonner aux événements du service
            this.subscribeToEvents();
          })
          .catch((err) => {
            this.handleError('Erreur d\'initialisation de l\'éditeur : ' + err.message);
          });
      }, 0);
    } catch (err: any) {
      this.handleError(err.message || 'Erreur inconnue');
    }
  }

  private subscribeToEvents(): void {
    // Événement document prêt
    this.onlyOfficeService.onDocumentReady.subscribe(() => {
      this.documentReady.emit();
    });

    // Événement erreur
    this.onlyOfficeService.onError.subscribe((msg) => {
      this.handleError(msg);
    });

    // Événement sauvegarde
    this.onlyOfficeService.onSave.subscribe(() => {
      this.saved.emit();
    });

    // Événement fermeture
    this.onlyOfficeService.onClose.subscribe(() => {
      this.closed.emit();
    });
  }

  private destroyEditor(): void {
    this.onlyOfficeService.destroyEditor();
    this.isEditorLoaded = false;
  }

  private handleError(message: string): void {
    this.isLoading = false;
    this.hasError = true;
    this.errorMessage = message;
    this.error.emit(message);
    console.error('OnlyOfficeViewer error:', message);
  }

  private getFileTypeFromName(fileName: string): string {
    const ext = fileName.split('.').pop()?.toLowerCase() || '';
    if (['doc', 'docx', 'odt', 'rtf', 'txt'].includes(ext)) return 'word';
    if (['xls', 'xlsx', 'ods', 'csv'].includes(ext)) return 'cell';
    if (['ppt', 'pptx', 'odp'].includes(ext)) return 'slide';
    if (['pdf'].includes(ext)) return 'pdf';
    return 'word'; // défaut
  }

  // Méthodes pour le template
  retry(): void {
    this.initializeViewer();
  }

  close(): void {
    this.closed.emit();
  }

  // Pour le débogage
  debugInfo(): void {
    const info = this.onlyOfficeService.getDebugInfo();
    console.log('OnlyOffice debug info:', info);
  }
}