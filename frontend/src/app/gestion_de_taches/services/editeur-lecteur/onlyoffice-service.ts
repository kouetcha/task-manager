import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable, Subject, fromEvent, merge } from 'rxjs';
import { map, filter, distinctUntilChanged } from 'rxjs/operators';

import { AuthService } from '../AuthService';
import { EditorMode, getFileType, OnlyOfficeConfig, OnlyOfficeCustomization } from '../../models/document.model';
import { FichierInfo } from '../../interfaces/base-entity-gestion';
import { environment } from '../../../../environments/environment';




// Interface pour les événements ONLYOFFICE
interface OnlyOfficeEditorEvent {
  event: string;
  data: any;
}

@Injectable({
  providedIn: 'root'
})
export class OnlyOfficeService {
  private documentServerUrl = environment.ONLY_OFFICE_URL; // ONLYOFFICE Document Server
  private apiUrl = environment.API_URL+'/tasksmanager'; // Votre backend API
  private scriptLoaded = false;
  private editorInstance: any = null;
  
  // Sujets pour les événements
  private scriptLoadedSubject = new BehaviorSubject<boolean>(false);
  private editorReadySubject = new BehaviorSubject<boolean>(false);
  private documentReadySubject = new Subject<void>();
  private documentModifiedSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new Subject<string>();
  private saveSubject = new Subject<void>();
  private closeSubject = new Subject<void>();
  private requestSaveAsSubject = new Subject<any>();
  private requestSaveSubject = new Subject<any>();
  private documentStateChangeSubject = new Subject<any>();
  private currentUser: any = null;
  constructor(
    @Inject(PLATFORM_ID) private platformId: any,

    private authService: AuthService
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.checkIfScriptAlreadyLoaded();
      this.setupGlobalEventListeners();
    }
  }

  // ========== ÉVÉNEMENTS OBSERVABLES ==========

  /**
   * Événement : Script ONLYOFFICE chargé
   */
  get onScriptLoaded(): Observable<boolean> {
    return this.scriptLoadedSubject.asObservable();
  }

  /**
   * Événement : Éditeur prêt
   */
  get onEditorReady(): Observable<boolean> {
    return this.editorReadySubject.asObservable();
  }

  /**
   * Événement : Document chargé dans l'éditeur
   */
  get onDocumentReady(): Observable<void> {
    return this.documentReadySubject.asObservable();
  }

  /**
   * Événement : Document modifié
   */
  get onDocumentModified(): Observable<boolean> {
    return this.documentModifiedSubject.asObservable();
  }

  /**
   * Événement : Erreur
   */
  get onError(): Observable<string> {
    return this.errorSubject.asObservable();
  }

  /**
   * Événement : Sauvegarde réussie
   */
  get onSave(): Observable<void> {
    return this.saveSubject.asObservable();
  }

  /**
   * Événement : Éditeur fermé
   */
  get onClose(): Observable<void> {
    return this.closeSubject.asObservable();
  }

  /**
   * Événement : Demande "Enregistrer sous"
   */
  get onRequestSaveAs(): Observable<any> {
    return this.requestSaveAsSubject.asObservable();
  }

  /**
   * Événement : Demande de sauvegarde
   */
  get onRequestSave(): Observable<any> {
    return this.requestSaveSubject.asObservable();
  }

  /**
   * Événement : Changement d'état du document
   */
  get onDocumentStateChange(): Observable<any> {
    return this.documentStateChangeSubject.asObservable();
  }

  /**
   * Événement combiné : Document modifié pour la première fois
   */
  get onFirstModification(): Observable<void> {
    return this.onDocumentModified.pipe(
      filter(isModified => isModified === true),
      distinctUntilChanged(),
      map(() => {})
    );
  }

  // ========== GESTION DU SCRIPT ONLYOFFICE ==========

  /**
   * Charge le script ONLYOFFICE
   */
  loadScript(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.scriptLoaded) {
        resolve();
        return;
      }

      if (document.getElementById('onlyoffice-api-script')) {
        this.scriptLoaded = true;
        this.scriptLoadedSubject.next(true);
        resolve();
        return;
      }

      const script = document.createElement('script');
      script.src = `${this.documentServerUrl}/web-apps/apps/api/documents/api.js`;
      script.type = 'text/javascript';
      script.id = 'onlyoffice-api-script';
      script.crossOrigin = 'anonymous';

      script.onload = () => {
        this.scriptLoaded = true;
        this.scriptLoadedSubject.next(true);
        console.log('ONLYOFFICE API script loaded successfully');
        resolve();
      };

      script.onerror = (error) => {
        console.error('Failed to load ONLYOFFICE script:', error);
        this.errorSubject.next('Échec du chargement du script ONLYOFFICE');
        reject(new Error('Failed to load ONLYOFFICE API'));
      };

      document.head.appendChild(script);
    });
  }

  /**
   * Vérifie si le script est déjà chargé
   */
  private checkIfScriptAlreadyLoaded(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.scriptLoaded = !!(window as any).DocsAPI;
      if (this.scriptLoaded) {
        this.scriptLoadedSubject.next(true);
      }
    }
  }

  /**
   * Configure les écouteurs d'événements globaux
   */
  private setupGlobalEventListeners(): void {
    // Écouter les messages de l'iframe ONLYOFFICE
    if (isPlatformBrowser(this.platformId)) {
      window.addEventListener('message', this.handleOnlyOfficeMessage.bind(this));
    }
  }

  /**
   * Gère les messages de ONLYOFFICE
   */
  private handleOnlyOfficeMessage(event: MessageEvent): void {
    // Vérifier l'origine du message
  const serverOrigin = new URL(this.documentServerUrl).origin; // 'http://192.168.190.159:4400'
  if (event.origin !== serverOrigin) {
    return;
  }
    try {
      const data = event.data;
      
      if (typeof data === 'string') {
        try {
          const parsedData = JSON.parse(data);
          this.processOnlyOfficeEvent(parsedData);
        } catch {
          // Si ce n'est pas du JSON, ignorer
        }
      } else if (typeof data === 'object') {
        this.processOnlyOfficeEvent(data);
      }
    } catch (error) {
      console.error('Error processing ONLYOFFICE message:', error);
    }
  }

  /**
   * Traite les événements ONLYOFFICE
   */
  private processOnlyOfficeEvent(eventData: any): void {
    if (!eventData || !eventData.type) return;

    console.log('ONLYOFFICE event received:', eventData.type, eventData);

    switch (eventData.type) {
      case 'documentReady':
        this.documentReadySubject.next();
        this.editorReadySubject.next(true);
        break;

      case 'documentModified':
        const isModified = eventData.data?.modified || false;
        this.documentModifiedSubject.next(isModified);
        break;

      case 'documentStateChange':
        this.documentStateChangeSubject.next(eventData.data);
        break;

      case 'requestSaveAs':
        this.requestSaveAsSubject.next(eventData.data);
        break;

      case 'requestSave':
        this.requestSaveSubject.next(eventData.data);
        break;

      case 'error':
        this.errorSubject.next(eventData.data?.message || 'Erreur ONLYOFFICE');
        break;

      case 'appReady':
        console.log('ONLYOFFICE application ready');
        break;

      case 'documentClose':
        this.closeSubject.next();
        break;

      case 'save':
        this.saveSubject.next();
        break;

      default:
        console.log('Unhandled ONLYOFFICE event:', eventData.type);
    }
  }

  /**
   * Vérifie la connexion au serveur Document Server
   */
async checkServerConnection(): Promise<boolean> {
  if (!isPlatformBrowser(this.platformId)) {
    return false;
  }

  try {
    const response = await fetch(`${this.documentServerUrl}/welcome/`, {
      method: 'GET',
      credentials: 'include', // envoie les cookies/session
    });

    if (!response.ok) {
      this.errorSubject.next('Serveur ONLYOFFICE injoignable');
      return false;
    }

    return true;
  } catch (error) {
    console.error('Document Server connection error:', error);
    this.errorSubject.next('Connexion au serveur ONLYOFFICE échouée');
    return false;
  }
}
  // ========== GESTION DE L'ÉDITEUR ==========

  /**
   * Crée une configuration pour ONLYOFFICE
   */
createConfig(
  document: FichierInfo ,
  editorMode: EditorMode,
  customization: Partial<OnlyOfficeCustomization> = {}
): OnlyOfficeConfig {
  // ======= VALIDATION CRITIQUE =======
  console.log('🔍 DEBUG createConfig appelé avec:', {
    type: typeof document,
  
    isObservable: document && typeof (document as any).subscribe === 'function',
    isPromise: document && typeof (document as any).then === 'function'
  });
  console.log("ONLYOFFICE SERVICE")
  console.log(editorMode)
  console.log(editorMode==='edit')

  // VÉRIFICATION: document ne doit PAS être un Observable
  if (document && typeof (document as any).subscribe === 'function') {
    console.error('🚨🚨🚨 ERREUR CRITIQUE: createConfig a reçu un Observable!');
    console.error('   Appelant devrait utiliser firstValueFrom() avant d\'appeler createConfig');
    console.error('   Stack trace:', new Error().stack);
    throw new Error('createConfig ne peut pas accepter un Observable. Utilisez firstValueFrom() ou await pour obtenir l\'objet document d\'abord.');
  }



  // Maintenant on sait que document n'est ni Observable ni Promise
  const doc = document;
  
  // Validation des paramètres requis
  if (!doc) {
    throw new Error('Document is required');
  }

  console.log('✅ Document valide pour createConfig:', {
    id: doc.id,
    name: doc.nomFichier,
    type: doc.type,
    url: doc.url || ''
  });

  if (!doc.id) {
    console.error('❌ Document ID manquant:', doc);
    throw new Error('Document ID is required');
  }

  if (!doc.url) {
    console.error('❌ Document URL manquante:', doc);
    throw new Error('Document URL is required');
  }

  // Déterminer le type de fichier
  const fileType = this.getFileExtension(doc.nomFichier);
  if (!fileType) {
    console.error('❌ Type de fichier non supporté:', doc.nomFichier);
    throw new Error(`Unsupported file type: ${doc.nomFichier}`);
  }

  // Déterminer le type de document ONLYOFFICE
  const documentType = this.mapDocumentType(doc.type);
  if (!documentType) {
    console.error('❌ Type de document non supporté:', doc.type);
    throw new Error(`Unsupported document type: ${doc.type}`);
  }

  const defaultCustomization: OnlyOfficeCustomization = {
    chat: true,
    comments: true,
    compactToolbar: false,
    zoom: 100,
    help: true,
    hideRulers: false,
    toolbarHideFileName: false,
    ...customization
  };

  // Obtenir l'utilisateur courant
  const user = this.authService.user$.subscribe(user => {
      this.currentUser = user;
     
    });
  // Vérifiez la structure de votre document
//console.log('Document complet:', this.currentUser);
//console.log('Champs disponibles:', Object.keys(doc));
  const userId = this.currentUser?.id || 'anonymous';
  const userName =  this.currentUser?.fullName || 'Invité';

  // Construire l'objet de configuration
  const config: OnlyOfficeConfig = {
    document: {
      fileType: fileType,
      key: this.generateDocumentKey(doc),
      title: doc.nomFichier,
      url: this.prepareDocumentUrl(doc.url),
      permissions: {
        edit: editorMode === 'edit',
        download: true,
        print: true,
        review: editorMode === 'edit',
        comment: editorMode === 'edit',
        fillForms: editorMode === 'edit',
        modifyFilter: editorMode === 'edit',
        modifyContentControl: editorMode === 'edit',
        copy: true,
      }
    },
    documentType: getFileType(doc.nomFichier),
    editorConfig: {
      mode: editorMode,
      lang: 'fr',
      callbackUrl: `${doc.callbackurl}`,
      customization: defaultCustomization,
      user: {
        id: `user-${userId}`,
        name: `${userName}`,
        groups: ['Utilisateurs']
      },
      embedded: {
        saveUrl: `${this.apiUrl}/documents/${doc.id}/content`,
        embedUrl: `${this.apiUrl}/documents/${doc.id}/embed`,
        shareUrl: `${this.apiUrl}/documents/${doc.id}/share`,
        toolbarDocked: 'top'
      },
      events: {
        onAppReady: () => {
          console.log('ONLYOFFICE app ready');
          this.emitEvent('appReady', {});
        },
        onDocumentStateChange: (event: any) => {
          console.log('Document state changed:', event);
          this.documentStateChangeSubject.next(event);
          this.emitEvent('documentStateChange', event);
        },
        onDocumentReady: () => {
          console.log('Document ready in editor');
          this.documentReadySubject.next();
          this.emitEvent('documentReady', {});
        },
        onError: (error: any) => {
          console.error('Editor error:', error);
          this.errorSubject.next(error?.message || 'Erreur éditeur');
          this.emitEvent('error', error);
        },
        onRequestSaveAs: (event: any) => {
          console.log('Request save as:', event);
          this.requestSaveAsSubject.next(event);
          this.emitEvent('requestSaveAs', event);
        },
        onRequestSave: (event: any) => {
          console.log('Request save:', event);
          this.requestSaveSubject.next(event);
          this.emitEvent('requestSave', event);
          this.saveDocument();
        },
        onSave: () => {
          console.log('Document saved');
          this.saveSubject.next();
          this.documentModifiedSubject.next(false);
          this.emitEvent('save', {});
        },
        onClose: () => {
          console.log('Editor closed');
          this.closeSubject.next();
          this.emitEvent('documentClose', {});
        }
      }
    },
    // token: this.jwtService.generateToken?.(doc.id.toString(), editorMode === 'edit'),
    height: '100%',
    width: '100%'
  };

  console.log('✅ Configuration ONLYOFFICE créée avec succès');
  console.log('🔴 CONFIG FINALE ONLYOFFICE:', JSON.stringify({
  documentType: config.documentType,
  fileType: config.document.fileType,
  docUrl: config.document.url,
  callbackUrl: config.editorConfig.callbackUrl,
  doc_type_input: doc.type
}, null, 2));
  console.log(config);
  return config;
}
  /**
   * Émet un événement ONLYOFFICE
   */
  private emitEvent(type: string, data: any): void {
    if (isPlatformBrowser(this.platformId)) {
      const event: OnlyOfficeEditorEvent = {
        event: type,
        data: data
      };
      
      // Émettre vers la fenêtre pour que les écouteurs globaux puissent le capturer
      window.dispatchEvent(new CustomEvent('onlyoffice-event', { 
        detail: event 
      }));
    }
  }

  /**
   * Initialise l'éditeur dans un conteneur
   */
  initializeEditor(containerId: string, config: OnlyOfficeConfig): Promise<any> {
    return new Promise((resolve, reject) => {
      if (!isPlatformBrowser(this.platformId)) {
        reject(new Error('OnlyOffice cannot be initialized on server side'));
        return;
      }

      if (!this.scriptLoaded) {
        reject(new Error('ONLYOFFICE script not loaded'));
        return;
      }

      const container = document.getElementById(containerId);
      if (!container) {
        reject(new Error(`Container ${containerId} not found`));
        return;
      }

      // Validation de la configuration
      try {
        this.validateConfig(config);
      } catch (validationError: any) {
        reject(new Error(`Invalid configuration: ${validationError.message}`));
        return;
      }

      try {
        // Nettoyer le conteneur
        container.innerHTML = '';
        
        // Créer un nouvel élément pour l'éditeur
        const editorDiv = document.createElement('div');
        editorDiv.id = `onlyoffice-editor-${Date.now()}`;
        editorDiv.style.width = '100%';
        editorDiv.style.height = '100%';
        editorDiv.style.display = 'block';
        container.appendChild(editorDiv);

        console.log('Initializing ONLYOFFICE editor with config:', config);
        
        // Initialiser l'éditeur
        this.editorInstance = new (window as any).DocsAPI.DocEditor(editorDiv.id, config);
        
        // Attendre que l'éditeur soit prêt
        const checkReady = setInterval(() => {
          if (this.editorInstance) {
            clearInterval(checkReady);
            this.editorReadySubject.next(true);
            console.log('ONLYOFFICE editor initialized successfully');
            resolve(this.editorInstance);
          }
        }, 100);

        // Timeout après 15 secondes
        setTimeout(() => {
          clearInterval(checkReady);
          if (!this.editorInstance) {
            const error = new Error('Editor initialization timeout');
            this.errorSubject.next('Initialisation de l\'éditeur timeout');
            reject(error);
          }
        }, 15000);

      } catch (error: any) {
        console.error('Error initializing editor:', error);
        this.errorSubject.next(`Échec de l'initialisation: ${error.message}`);
        reject(new Error(`Failed to initialize editor: ${error.message}`));
      }
    });
  }

  /**
   * Valide la configuration ONLYOFFICE
   */
  private validateConfig(config: OnlyOfficeConfig): void {
    const requiredFields = [
      { path: 'document', name: 'document' },
      { path: 'document.fileType', name: 'fileType' },
      { path: 'document.key', name: 'key' },
      { path: 'document.title', name: 'title' },
      { path: 'document.url', name: 'url' },
      { path: 'documentType', name: 'documentType' },
      { path: 'editorConfig', name: 'editorConfig' },
      { path: 'editorConfig.callbackUrl', name: 'callbackUrl' },
      { path: 'editorConfig.lang', name: 'lang' }
    ];

    const missingFields: string[] = [];
    
    requiredFields.forEach(field => {
      const value = this.getNestedValue(config, field.path);
      if (value === undefined || value === null || value === '') {
        missingFields.push(field.name);
      }
    });

    if (missingFields.length > 0) {
      throw new Error(`Missing required fields: ${missingFields.join(', ')}`);
    }

    // Vérifier les types de fichiers supportés
    const supportedFileTypes = [
      'doc', 'docx', 'odt', 'txt', 'rtf',
      'xls', 'xlsx', 'ods', 'csv',
      'ppt', 'pptx', 'odp',
      'pdf'
    ];
    
    if (!supportedFileTypes.includes(config.document.fileType.toLowerCase())) {
      throw new Error(`Unsupported file type: ${config.document.fileType}. Supported types: ${supportedFileTypes.join(', ')}`);
    }
  }

  /**
   * Récupère une valeur imbriquée d'un objet
   */
  private getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((o, p) => o && o[p], obj);
  }

  /**
   * Détruit l'éditeur
   */
  destroyEditor(): void {
    if (this.editorInstance) {
      try {
        // ONLYOFFICE n'a pas de méthode destroy, on nettoie juste le DOM
        const editorElement = document.querySelector('[id^="onlyoffice-editor-"]');
        if (editorElement) {
          editorElement.remove();
        }
        this.editorInstance = null;
        this.editorReadySubject.next(false);
        this.documentModifiedSubject.next(false);
        console.log('ONLYOFFICE editor destroyed');
      } catch (error) {
        console.warn('Error destroying editor:', error);
      }
    }
  }

  /**
   * Force la sauvegarde du document
   */
  saveDocument(): void {
    if (this.editorInstance && typeof this.editorInstance.setDocumentSaved === 'function') {
      try {
        this.editorInstance.setDocumentSaved();
        this.saveSubject.next();
        this.documentModifiedSubject.next(false);
      } catch (error) {
        console.error('Error saving document:', error);
        this.errorSubject.next('Erreur lors de la sauvegarde');
      }
    } else {
      this.errorSubject.next('Éditeur non initialisé pour la sauvegarde');
    }
  }

  /**
   * Marque le document comme modifié
   */
  setDocumentModified(isModified: boolean = true): void {
    this.documentModifiedSubject.next(isModified);
  }

  // ========== UTILITAIRES ==========

  /**
   * Prépare l'URL du document pour ONLYOFFICE
   */
  private prepareDocumentUrl(url: string): string {
    // Si l'URL est relative, la transformer en absolue
    if (url && !url.startsWith('http')) {
      return `${this.apiUrl}${url.startsWith('/') ? url : '/' + url}`;
    }
    return url;
  }

  /**
   * Génère une clé unique pour le document
   */
  private generateDocumentKey(document: FichierInfo): string {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substr(2, 9);
    const documentId = document.id || document.nomFichier;
    return `doc-${documentId}-${timestamp}-${random}`;
  }

  /**
   * Obtient l'extension du fichier
   */
  private getFileExtension(filename: string): string {
    const extension = filename.toLowerCase().split('.').pop();
    
    // Mapper les extensions aux types supportés par ONLYOFFICE
    const extensionMap: { [key: string]: string } = {
      'doc': 'doc',
      'docx': 'docx',
      'odt': 'odt',
      'txt': 'txt',
      'rtf': 'rtf',
      'xls': 'xls',
      'xlsx': 'xlsx',
      'ods': 'ods',
      'csv': 'csv',
      'ppt': 'ppt',
      'pptx': 'pptx',
      'odp': 'odp',
      'pdf': 'pdf'
    };
    
    return extensionMap[extension || ''] || extension || 'docx';
  }

  /**
   * Mappe le type de document vers ONLYOFFICE
   */
    private mapDocumentType(type: string): string {
      switch (type.toLowerCase()) {
        case 'word':
        case 'text':
        case 'doc':
        case 'docx':
          return 'word';         // ✅
        case 'cell':
        case 'spreadsheet':
        case 'xls':
        case 'xlsx':
          return 'cell';         // ✅
        case 'slide':
        case 'presentation':
        case 'ppt':
        case 'pptx':
          return 'slide';        // ✅
        case 'pdf':
          return 'pdf';
        default:
          return 'word';
      }
    }

  // ========== MÉTHODES DE DÉBOGAGE ==========

  /**
   * Vérifie la configuration et retourne les problèmes
   */
  debugConfig(config: OnlyOfficeConfig): { valid: boolean; issues: string[] } {
    const issues: string[] = [];
    
    if (!config.document) issues.push('document object is missing');
    if (!config.document?.fileType) issues.push('document.fileType is missing');
    if (!config.document?.key) issues.push('document.key is missing');
    if (!config.document?.title) issues.push('document.title is missing');
    if (!config.document?.url) issues.push('document.url is missing');
    if (!config.documentType) issues.push('documentType is missing');
    if (!config.editorConfig) issues.push('editorConfig is missing');
    if (!config.editorConfig?.callbackUrl) issues.push('editorConfig.callbackUrl is missing');
    if (!config.editorConfig?.lang) issues.push('editorConfig.lang is missing');
    
    return {
      valid: issues.length === 0,
      issues: issues
    };
  }

  /**
   * Récupère les informations de débogage
   */
  getDebugInfo(): any {
    return {
      scriptLoaded: this.scriptLoaded,
      editorInitialized: !!this.editorInstance,
      documentServerUrl: this.documentServerUrl,
      apiUrl: this.apiUrl,
      platform: isPlatformBrowser(this.platformId) ? 'browser' : 'server',
      onlyofficeApiAvailable: isPlatformBrowser(this.platformId) ? !!(window as any).DocsAPI : false
    };
  }

  // ========== GETTERS ==========

  /**
   * Vérifie si le script est chargé
   */
  isScriptLoaded(): boolean {
    return this.scriptLoaded;
  }

  /**
   * Vérifie si l'éditeur est initialisé
   */
  isEditorInitialized(): boolean {
    return !!this.editorInstance;
  }

  /**
   * Vérifie si le document est modifié
   */
  isDocumentModified(): boolean {
    return this.documentModifiedSubject.value;
  }

  /**
   * Retourne l'instance de l'éditeur
   */
  getEditorInstance(): any {
    return this.editorInstance;
  }

  /**
   * Configure l'URL du serveur Document Server
   */
  setDocumentServerUrl(url: string): void {
    this.documentServerUrl = url;
  }

  /**
   * Récupère l'URL du serveur Document Server
   */
  getDocumentServerUrl(): string {
    return this.documentServerUrl;
  }

  /**
   * Configure l'URL de l'API backend
   */
  setApiUrl(url: string): void {
    this.apiUrl = url;
  }

  /**
   * Récupère l'URL de l'API backend
   */
  getApiUrl(): string {
    return this.apiUrl;
  }

  /**
   * Met à jour le zoom de l'éditeur
   */
  updateZoom(zoomLevel: number): void {
    if (this.editorInstance && typeof this.editorInstance.setZoom === 'function') {
      try {
        this.editorInstance.setZoom(zoomLevel);
      } catch (error) {
        console.error('Error updating zoom:', error);
      }
    }
  }

  /**
   * Met à jour le mode de l'éditeur
   */
  updateMode(mode: EditorMode): void {
    if (this.editorInstance && typeof this.editorInstance.setMode === 'function') {
      try {
        this.editorInstance.setMode(mode);
      } catch (error) {
        console.error('Error updating mode:', error);
      }
    }
  }
}