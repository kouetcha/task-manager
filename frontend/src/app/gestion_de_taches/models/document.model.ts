// Interfaces pour les documents
export interface DocumentInfo {
  id: number;
  name: string;
  originalName?: string;
  userFullName: string;
  url: string;
  type: DocumentType;
  size?: number;
  formattedSize?: string;
  lastModified?: Date;
  isModified?: boolean;
  createdAt?: Date;
  ownerId?: string;
  fileCode: string;
  dossierId:number;
  dossierPath?:string;
  permissions?: DocumentPermissions;
  [key: string]: any;
}

export interface DocumentUpload {
  file: File;
  name?: string;
  type?: DocumentType;
  folderId?: number;
   userId: number;
}

export interface DocumentCreate {
  name: string;
  type: DocumentType;
  extension: string;
  userId: number;
  template?: string;
  dossierId?: string;
}

export interface DocumentSave {
  documentId: number;
  key: string;
  url?: string;
  changesurl?: string;
  error:string
}

export interface DocumentSaveAs {
  documentId: number;
  newName: string;
  dossierId?: string;
}

export interface DocumentPermissions {
  edit: boolean;
  download: boolean;
  print: boolean;
  review: boolean;
  comment: boolean;
  fillForms: boolean;
  modifyFilter: boolean;
  copy:boolean;
  modifyContentControl: boolean;
  delete?:boolean;
  partage?:boolean;
  read?:boolean;
}

export interface RecentDocument {
  id: number;
  name: string;
  url: string;
  type: DocumentType;
  lastOpened: Date;
  openedCount: number;
}

export interface UploadProgress {
  percentage: number;
  loaded: number;
  total: number;
  speed?: number;
  estimatedTime?: number;
}



// Types
export type DocumentType = 'word' | 'cell' | 'slide' | 'pdf' | 'unknown';
export type EditorMode = 'edit' | 'view';

// Configuration ONLYOFFICE
export interface OnlyOfficeConfig {
  document: {
    fileType: string;
    key: string;
    title: string;
    url: string;
    permissions?: DocumentPermissions;
  };
  documentType: string;
  editorConfig: {
    mode: EditorMode;
    lang: string;
    callbackUrl?: string;
    customization: OnlyOfficeCustomization;
    embedded: {
      saveUrl: string;
      embedUrl: string;
      shareUrl: string;
      toolbarDocked: 'top' | 'bottom';
    };
    user: {
      id: string;
      name: string;
      groups?: string[];
    };
    events?: OnlyOfficeEvents; // ICI : events doit être dans editorConfig
  };
  token?: string;
  height?: string;
  width?: string;
}
export interface OnlyOfficeCustomization {
  chat: boolean;
  comments: boolean;
  compactToolbar: boolean;
  zoom: number;
  help: boolean;
  hideRulers?: boolean;
  toolbarHideFileName?: boolean;
  [key: string]: any;
}

export interface OnlyOfficeEvents {
  onAppReady?: () => void;
  onSave?: () => void;
  onClose?: () => void;
  onDocumentReady?: () => void;
  onDocumentStateChange?: (event: any) => void;
  onError?: (error: any) => void;
  onRequestSaveAs?: (event: any) => void;
  onRequestSave?: (event: any) => void;
  onInfo?: (data: any) => void;
  onWarning?: (data: any) => void;
}

// Réponses API
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface UploadResponse {
  id: number;
  name: string;
  url: string;
  size: number;
  type: DocumentType;
  fileCode: string;
  userFullName: string;
  dossierId:number
}

export interface CreateResponse {
  id: number;
  name: string;
  url: string;
  size: number;
  type: DocumentType;
  fileCode: string;
  userFullName: string;
  dossierId:number;
}

// JWT Payload
export interface JwtPayload {
  document_id: string;
  permissions: {
    edit: boolean;
    download: boolean;
    print: boolean;
  };
  user_id: string;
  user_name?: string;
  exp: number;
  iat: number;
}

/**
 * Types de documents supportés par OnlyOffice
 */
export enum DocType {
  WORD = 'word',
  CELL = 'cell',
  SLIDE = 'slide',
  PDF = 'pdf',
  DIAGRAM = 'diagram',
  UNKNOWN = 'unknown'
}

/**
 * Détermine le type de document en fonction de l'extension du fichier
 * @param fileName Nom du fichier avec extension
 * @returns Le type de document (word, cell, slide, pdf, diagram, unknown)
 */
export function getFileType(fileName: string): DocType {
  if (!fileName) return DocType.UNKNOWN;
  
  const extension = getFileExtension(fileName).toLowerCase();
  
  // Documents texte
  const wordExtensions = [
    'doc', 'docm', 'docx', 'dot', 'dotm', 'dotx', 'epub', 'fb2', 
    'fodt', 'hml', 'htm', 'html', 'md', 'hwp', 'hwpx', 'mht', 'mhtml', 
    'odt', 'ott', 'pages', 'rtf', 'stw', 'sxw', 'txt', 'wps', 'wpt', 'xml'
  ];
  
  // Tableurs
  const cellExtensions = [
    'csv', 'et', 'ett', 'fods', 'numbers', 'ods', 'ots', 'sxc', 
    'xls', 'xlsb', 'xlsm', 'xlsx', 'xlt', 'xltm', 'xltx', 'xml'
  ];
  
  // Présentations
  const slideExtensions = [
    'dps', 'dpt', 'fodp', 'key', 'odg', 'odp', 'otp', 'pot', 'potm', 
    'potx', 'pps', 'ppsm', 'ppsx', 'ppt', 'pptm', 'pptx', 'sxi'
  ];
  
  // PDF et similaires
  const pdfExtensions = ['djvu', 'oxps', 'pdf', 'xps'];
  
  // Diagrammes
  const diagramExtensions = ['vsdm', 'vsdx', 'vssm', 'vssx', 'vstm', 'vstx'];
  
  if (wordExtensions.includes(extension)) {
    return DocType.WORD;
  }
  
  if (cellExtensions.includes(extension)) {
    return DocType.CELL;
  }
  
  if (slideExtensions.includes(extension)) {
    return DocType.SLIDE;
  }
  
  if (pdfExtensions.includes(extension)) {
    return DocType.PDF;
  }
  
  if (diagramExtensions.includes(extension)) {
    return DocType.DIAGRAM;
  }
  
  return DocType.UNKNOWN;
}

/**
 * Extrait l'extension d'un nom de fichier
 * @param fileName Nom du fichier
 * @returns L'extension du fichier sans le point
 */
export function getFileExtension(fileName: string): string {
  if (!fileName) return '';
  
  const lastDotIndex = fileName.lastIndexOf('.');
  
  if (lastDotIndex === -1 || lastDotIndex === fileName.length - 1) {
    return '';
  }
  
  return fileName.substring(lastDotIndex + 1).toLowerCase();
}

/**
 * Vérifie si un fichier est éditable dans OnlyOffice
 * @param fileName Nom du fichier
 * @returns true si le fichier est éditable
 */
export function isEditableInOnlyOffice(fileName: string): boolean {
  const fileType = getFileType(fileName);
  return fileType !== DocType.UNKNOWN;
}

/**
 * Vérifie si un fichier est un document texte éditable
 * @param fileName Nom du fichier
 * @returns true si c'est un document texte
 */
export function isWordDocument(fileName: string): boolean {
  return getFileType(fileName) === DocType.WORD;
}

/**
 * Vérifie si un fichier est un tableur éditable
 * @param fileName Nom du fichier
 * @returns true si c'est un tableur
 */
export function isCellDocument(fileName: string): boolean {
  return getFileType(fileName) === DocType.CELL;
}

/**
 * Vérifie si un fichier est une présentation éditable
 * @param fileName Nom du fichier
 * @returns true si c'est une présentation
 */
export function isSlideDocument(fileName: string): boolean {
  return getFileType(fileName) === DocType.SLIDE;
}

/**
 * Vérifie si un fichier est un PDF (lecture seule)
 * @param fileName Nom du fichier
 * @returns true si c'est un PDF
 */
export function isPdfDocument(fileName: string): boolean {
  return getFileType(fileName) === DocType.PDF;
}

/**
 * Vérifie si un fichier est un diagramme
 * @param fileName Nom du fichier
 * @returns true si c'est un diagramme
 */
export function isDiagramDocument(fileName: string): boolean {
  return getFileType(fileName) === DocType.DIAGRAM;
}