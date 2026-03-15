import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EditableTacheDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { Tache } from '../../models/tache.model';
import { TacheService } from '../../services/tache.service';
import { AuthService } from '../../services/AuthService';
import { NotificationService } from '../../services/notification.service';
import { TacheDetail } from '../tache-detail/tache-detail';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FichierService } from '../../services/fichier-service';
import { switchMap } from 'rxjs';
import { CommentaireList } from '../commentaires/commentaire-list/commentaire-list';

@Component({
  selector: 'app-tache-section',
  imports: [TacheDetail,FormsModule,CommonModule],
  templateUrl: './tache-section.html',
  styleUrl: './tache-section.css',
})
export class TacheSection implements OnInit{
  @Input() activiteId!: number;

  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<{ file: FichierInfo; type: string; id: number }>();
  @Output() tacheChanged = new EventEmitter<void>(); // pour notifier le parent si besoin
  @Output() tacheSelected = new EventEmitter<Tache>();
  @Output() tacheSelectedDelete = new EventEmitter<Tache>();
   @Input() currentUserId:number=0;
   @Input() currentUserEmail!:string;
   @Input() currentUserProfilePicture:string='';
  taches: Tache[] = [];
  @Input() selectedTache?: Tache;
  showTacheForm = false;
  editingTache?: Tache;
  tacheForm: any = {
    designation: '',
    description: '',
    dateDebut: new Date(),
    dateFin: new Date(),
    status: 'EN_ATTENTE',
    emailsString: ''
  };
  selectedFilesTache: File[] = [];
  loading = false;

  constructor(
    private tacheService: TacheService,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef,
    private fichierService:FichierService
  ) {}

  ngOnInit(): void {
   
    this.loadTaches();
  }

  loadTaches(): void {
    this.loading = true;
    this.tacheService.getByActiviteAndEmail(this.activiteId,this.currentUserEmail).subscribe({
      next: (data) => {
        console.log("DATA etByActiviteAndEmail ");
        console.log("this.activiteId");
        console.log(this.activiteId)
        console.log("Email ");
         console.log(this.currentUserEmail);
        console.log(data);
        this.taches = data;
        if (this.taches.length > 0 && !this.selectedTache) {
          this.selectedTache = this.taches[0];
          this.tacheSelected.emit(this.selectedTache);
        }
        this.loading = false;
        this.cdr.detectChanges()
      },
      error: (err) => {
        console.error('Erreur chargement activités', err);
        this.loading = false;
      }
    });
  }

  selectTache(tache: Tache): void {
    this.selectedTache = tache;
    this.cdr.detectChanges()
    console.log("Tache selectionnée")
    this.tacheSelected.emit(this.selectedTache);
    console.log(this.selectedTache)
  }
onDeleteFile(file: any): void {
  this.deleteFile.emit({ file, type: 'tache', id: this.selectedTache!.id });
}
  onCreateTache(): void {
    this.showTacheForm = true;
    this.editingTache = undefined;
    this.tacheForm = {
      designation: '',
      description: '',
      dateDebut: new Date(),
      dateFin: new Date(),
      status: 'EN_ATTENTE',
      emailsString: ''
    };
    this.selectedFilesTache = [];
  }

  onEditTache(tache: Tache): void {
    this.showTacheForm = true;
    this.editingTache = tache;
    this.tacheForm = {
      designation: tache.designation,
      description: tache.description || '',
      dateDebut: this.formatDateForInput(new Date(tache.dateDebut)),
      dateFin: this.formatDateForInput(new Date(tache.dateFin)),
      status: tache.status,
      emailsString: tache.emails?.map(e => e.email).join(', ') || ''
    };
    this.selectedFilesTache = [];
  }
       private formatDateForInput(date: Date | string | undefined): string {
        if (!date) return '';
        const d = new Date(date);
        // Retourne YYYY-MM-DD
        return d.toISOString().split('T')[0];
      }
   

  onDeleteTache(tache: Tache): void {
    if (confirm(`Supprimer l'activité "${tache.designation}" ?`)) {
      this.tacheService.delete(tache.id).subscribe({
        next: () => {
          this.taches = this.taches.filter(a => a.id !== tache.id);
          if (this.selectedTache?.id === tache.id) {
            this.selectedTache = this.taches.length > 0 ? this.taches[0] : undefined;
          }
          this.notification.success('Activité supprimée');
          this.tacheChanged.emit();
          this.tacheSelectedDelete.emit(this.selectedTache);
        },
        error: (err) => {
          console.error('Erreur suppression', err);
          this.notification.error('Erreur lors de la suppression');
        }
      });
    }
  }

  onSaveTache(): void {
    const dto: any = {
      designation: this.tacheForm.designation,
      description: this.tacheForm.description || undefined,
      dateDebut: this.tacheForm.dateDebut,
      dateFin: this.tacheForm.dateFin,
      status: this.tacheForm.status,
      emails: this.parseEmails(this.tacheForm.emailsString),
      createurId:this.currentUserId
    };

    if (this.editingTache) {
      // Mise à jour
      this.tacheService.update(this.editingTache.id, dto).subscribe({
        next: (updated) => {
          const index = this.taches.findIndex(a => a.id === updated.id);
          if (index !== -1) this.taches[index] = updated;
          if (this.selectedTache?.id === updated.id) this.selectedTache = updated;
          this.cancelTacheForm();
          this.notification.success('Tache mise à jour');
          this.tacheChanged.emit();
          this.tacheSelected.emit(this.selectedTache);
        },
        error: (err) => {
          console.error('Erreur mise à jour', err);
          this.notification.error('Erreur lors de la mise à jour');
        }
      });
    } else {
      // Création
      dto.createurId = this.currentUserId;
      this.tacheService.create(this.activiteId, dto, this.selectedFilesTache).subscribe({
        next: (created) => {
          this.taches.push(created);
          this.selectedTache = created;
          this.cancelTacheForm();
          this.notification.success('Activité créée');
          this.tacheChanged.emit();
          this.tacheSelected.emit(this.selectedTache);
        },
        error: (err) => {
          console.error('Erreur création', err);
          this.notification.error('Erreur lors de la création');
        }
      });
    }
  }

  cancelTacheForm(): void {
    this.showTacheForm = false;
    this.editingTache = undefined;
    this.tacheForm = {};
    this.selectedFilesTache = [];
  }

  parseEmails(emailsString: string): string[] {
    return emailsString.split(',').map(email => email.trim()).filter(e => e.length > 0);
  }

  onFilesSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.selectedFilesTache = Array.from(files);
    }
  }

  scrollCarousel(direction: number): void {
    const carousel = document.querySelector('.overflow-x-auto') as HTMLElement;
    if (carousel) {
      const scrollAmount = 300;
      carousel.scrollBy({ left: direction * scrollAmount, behavior: 'smooth' });
    }
  }

  getStatusColor(status: string): string {
    const colors: any = {
      'EN_ATTENTE': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      'EN_COURS': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      'TERMINE': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      'ANNULE': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels: any = {
      'EN_ATTENTE': 'En attente',
      'EN_COURS': 'En cours',
      'TERMINE': 'Terminé',
      'ANNULE': 'Annulé'
    };
    return labels[status] || status;
  }


  onApplieELementChange(editableChamps: EditableTacheDto) {
  let observable;
  let messageSuccess = '';
  let messageError = '';

  switch (editableChamps.type) {
    case 'DESIGNATION':
      observable = this.tacheService.updateTacheDesignation(
        editableChamps.tache.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Désignation mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la désignation.';
      break;

    case 'DESCRIPTION':
      observable = this.tacheService.updateTacheDescription(

        editableChamps.tache.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Description mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la description.';
      break;

    case 'DATE_DEBUT':
      observable = this.tacheService.updateTacheDateDebut(
        editableChamps.tache.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de début mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de début.';
      break;

    case 'DATE_FIN':
      observable = this.tacheService.updateTacheDateFin(
  
        editableChamps.tache.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de fin mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de fin.';
      break;

    default:
      return;
  }

  observable.subscribe({
    next: (tacheMisAJour) => {
     
    this.selectedTache=tacheMisAJour;
      this.notification.success(messageSuccess)

    },
    error: (err) => {
      console.error(err);
     
      this.notification.error(messageError)
    }
  });

}

  updateTache(liste: Tache[], tacheMisAJour: Tache) {
  const index = liste.findIndex(p => p.id === tacheMisAJour.id);
  if (index !== -1) {
    liste[index] = tacheMisAJour;
  this.tacheSelected.emit(this.selectedTache);
  }
}
// Remplacer onAddFiles par cette version
onAddFiles( files: File[],tache: Tache): void {
  this.fichierService.uploadFiles('TACHE', tache.id, files).pipe(
    switchMap(() => this.fichierService.listFiles('TACHE', tache.id))
  ).subscribe({
    next: (fichiers) => {
      const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
        id: f.id,
        nomFichier: f.nomFichier ?? '',
        url: f.url ?? '',
        type: f.type ?? '',
        callbackurl:f.callbackurl??''
      }));

const updateTache = (list: Tache[]) => {
  const idx = list.findIndex(p => p.id === tache.id);
  if (idx !== -1) {
    // Object.assign préserve l'instance de classe (et donc le getter simpleEmails)
    list[idx] = Object.assign({}, list[idx], { fichiers: updatedFichiers });
    this.selectedTache=list[idx];
    this.tacheSelected.emit(this.selectedTache);
  }
};

      updateTache(this.taches);
      this.tacheChanged.emit()
      
      this.cdr.detectChanges();
    },
    error: (err) => {
      console.error('Erreur upload/récupération fichiers:', err);
      this.notification.error("Erreur lors de l'upload");
    }
  });
}

}
