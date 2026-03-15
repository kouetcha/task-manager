import { ChangeDetectorRef, Component, HostListener, signal } from '@angular/core';
import { EditableDto, FichierInfo, Projet } from '../../interfaces/base-entity-gestion';
import { Activite } from '../../models/activite.model';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjetService } from '../../services/projet-service';
import { ActiviteService } from '../../services/activite.service';
import { TacheService } from '../../services/tache.service';
import { AuthService } from '../../services/AuthService';
import { MatDialog } from '@angular/material/dialog';
import { FichierService } from '../../services/fichier-service';
import { ProjetCardDetail } from '../../components/projet-details/projet-card-detail/projet-card-detail';
import { NotificationService } from '../../services/notification.service';
import { OnlyOfficeViewer } from '../../components/cards/onlyoffice-viewer/onlyoffice-viewer';
import { PdfModal } from '../../components/cards/pdf-modal/pdf-modal';
import { MaterialModule } from '../../material.module';
import { SafeResourceUrl } from '@angular/platform-browser';
import { Observable, switchMap } from 'rxjs';
import { ActiviteDetail } from '../../components/activite-detail/activite-detail';
import { ActiviteSection } from '../../components/activite-section/activite-section';
import { TacheSection } from '../../components/tache-section/tache-section';
import { User } from '../../models/user';
import { Tache } from '../../models/tache.model';

@Component({
  selector: 'app-projet-detail',
  imports: [ProjetCardDetail,OnlyOfficeViewer,PdfModal,MaterialModule,ActiviteSection,TacheSection],
  templateUrl: './projet-detail.html',
  styleUrl: './projet-detail.css',
})
export class ProjetDetail {

    projetId!: number;
    projet?: Projet;
    activiteSelected?:Activite
    tacheSelected?:Tache;
    activites: Activite[] = [];
    loading = true;
    isPdfModalOpen = false;
     pdfPreviewUrl: SafeResourceUrl|string  ='';
 
  fileName: string = '';
  isOnlyOfficeModalOpen = false;
  onlyOfficeFileUrl: string | null = null;
  fichierInfo:FichierInfo|null=null;
  currentDocumentId?: number;
  isCollapsed= false;
  private currentUserId !:number; 
private user = signal<User | null>(null);
getUser(){
  return this.user();
}

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private activiteService: ActiviteService,
    private tacheService: TacheService,
    private authService: AuthService,
    private dialog: MatDialog,
    private fichierService:FichierService,
    private notification:NotificationService,
    private cdr: ChangeDetectorRef
  ) {
   this.authService.user$.subscribe((user) => {  
      if (user) {
      this.currentUserId = user.id;
      this.user.set(user);
    }});
  }

  ngOnInit(): void {
   
    
  

     this.projetId = +this.route.snapshot.paramMap.get('id')!;
    this.loadProjet();

  }
  getCurrentUserId(){
    return this.currentUserId;
  }

  goBack(): void {
    this.router.navigate(['/app/projets']);
  }
  onActiviteSelected(activite:Activite){
    this.activiteSelected=activite;
  }
   onTacheSelected(tache:Tache){
    this.tacheSelected=tache;
  }
  onActiviteSelectedDelete(activite:Activite){
    if(this.activiteSelected&&this.activiteSelected.id==activite.id)
 
        this.activiteSelected=undefined

  }
    onTacheSelectedDelete(tache:Tache){
    if(this.tacheSelected&&this.tacheSelected.id==tache.id)
 
        this.tacheSelected=undefined

  }
  loadProjet(): void {
    this.projetService.getProjetById(this.projetId).subscribe({
      next: (data) => {
        this.projet = data;
        this.loading = false;
         this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement projet', err);
        this.loading = false;
      }
    });
  }
onApplieELementChange(editableChamps: EditableDto) {
  let observable;
  let messageSuccess = '';
  let messageError = '';

  switch (editableChamps.type) {
    case 'DESIGNATION':
      observable = this.projetService.updateProjetDesignation(
        editableChamps.projet.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Désignation mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la désignation.';
      break;

    case 'DESCRIPTION':
      observable = this.projetService.updateProjetDescription(
        editableChamps.projet.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Description mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la description.';
      break;

    case 'DATE_DEBUT':
      observable = this.projetService.updateProjetDateDebut(
        editableChamps.projet.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de début mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de début.';
      break;

    case 'DATE_FIN':
      observable = this.projetService.updateProjetDateFin(
        editableChamps.projet.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de fin mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de fin.';
      break;

    default:
      return;
  }

  observable.subscribe({
    next: (projetMisAJour) => {
     
    this.projet=projetMisAJour;
      this.notification.success(messageSuccess)

    },
    error: (err) => {
      console.error(err);
     
      this.notification.error(messageError)
    }
  });
}
  onViewFile(file: FichierInfo): void {
     // Construire l'URL de visualisation (à adapter selon votre API)
     const fileUrl =file.url; 
     const extension = file.nomFichier.split('.').pop()?.toLowerCase();
 
     this.fileName = file.nomFichier;
 
     if (extension === 'pdf') {
       this.pdfPreviewUrl = fileUrl;
       this.isPdfModalOpen = true;
     } else {
   this.onlyOfficeFileUrl = fileUrl;
   this.fichierInfo=file;
   this.fileName = file.nomFichier;
   this.currentDocumentId = file.id;
   this.isOnlyOfficeModalOpen = true;
 }
 
   }
   onViewerError(error: string): void {
   console.error('Erreur du viewer ONLYOFFICE:', error);
   // Afficher une notification à l'utilisateur
 }
  

  onOnlyOfficeModalClosed(): void {
    this.isOnlyOfficeModalOpen = false;
    this.onlyOfficeFileUrl = null;
    this.fichierInfo=null;
  }
 
 onDeleteFile(file: FichierInfo, parentType: 'projet' | 'activite' | 'tache', parentId: number): void {
  if (confirm(`Supprimer le fichier "${file.nomFichier}" ?`)) {
    let deleteObservable: Observable<any>;

    switch (parentType) {
      case 'projet':
        deleteObservable = this.projetService.deleteFichier(parentId, file.id);
        break;
      case 'activite':
        deleteObservable = this.activiteService.deleteFichier(parentId, file.id);
        break;
      case 'tache':
        deleteObservable = this.tacheService.deleteFichier(parentId, file.id);
        break;
      default:
        return;
    }

    deleteObservable.subscribe({
      next: () => {
        switch (parentType) {
          case 'projet':
            if (this.projet) {
              const updatedFichiers = (this.projet.fichiers ?? []).filter(f => f.id !== file.id);
              this.projet = Object.assign(new Projet(), this.projet, { fichiers: updatedFichiers });
            }
            break;

          case 'activite':
            if (this.activiteSelected) {
              const updatedFichiers = (this.activiteSelected.fichiers ?? []).filter(f => f.id !== file.id);
              this.activiteSelected = Object.assign({}, this.activiteSelected, { fichiers: updatedFichiers });
            }
            break;

          case 'tache':
             if (this.tacheSelected) {
              const updatedFichiers = (this.tacheSelected.fichiers ?? []).filter(f => f.id !== file.id);
              this.tacheSelected = Object.assign({}, this.tacheSelected, { fichiers: updatedFichiers });
            }
            break;
        }

        this.notification.success('Fichier supprimé avec succès');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur suppression fichier', err);
        this.notification.error('Erreur lors de la suppression');
      }
    });
  }
}
    onPdfModalClosed(): void {
    this.isPdfModalOpen = false;
    this.pdfPreviewUrl = '';
  }

  onAddFiles(projet: Projet, files: File[]): void {
    this.fichierService.uploadFiles('PROJET', projet.id, files).pipe(
      switchMap(() => this.fichierService.listFiles('PROJET', projet.id))
    ).subscribe({
      next: (fichiers) => {
        const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
          id: f.id,
          nomFichier: f.nomFichier ?? '',
          url: f.url ?? '',
          type: f.type ?? '',
          callbackurl:f.callbackurl??''
        }));
        this.projet=Object.assign(new Projet(), this.projet, { fichiers: updatedFichiers });
  
     
        this.notification.success("Fichier(s) ajouté(s) avec succès");
      
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur upload/récupération fichiers:', err);
        this.notification.error("Erreur lors de l'upload");
      }
    });
  }

  showScrollTop = false;

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.showScrollTop = window.scrollY > 300;
    this.cdr.detectChanges()
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  scrollTo(elementId: string) {
    const element = document.getElementById(elementId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      this.cdr.detectChanges()
    }
  }


}
