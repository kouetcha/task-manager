import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EditableActivieDto, FichierInfo } from '../../interfaces/base-entity-gestion';
import { Activite } from '../../models/activite.model';
import { ActiviteService } from '../../services/activite.service';
import { AuthService } from '../../services/AuthService';
import { NotificationService } from '../../services/notification.service';
import { ActiviteDetail } from '../activite-detail/activite-detail';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FichierService } from '../../services/fichier-service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-activite-section',
  imports: [ActiviteDetail,FormsModule,CommonModule],
  templateUrl: './activite-section.html',
  styleUrl: './activite-section.css',
})
export class ActiviteSection implements OnInit{
  @Input() projetId!: number;

  @Output() viewFile = new EventEmitter<FichierInfo>();
  @Output() deleteFile = new EventEmitter<{ file: FichierInfo; type: string; id: number }>();
  @Output() activiteChanged = new EventEmitter<void>(); // pour notifier le parent si besoin
  @Output() activiteSelected = new EventEmitter<Activite>();
  @Output() activiteSelectedDelete = new EventEmitter<Activite>();
  @Input() currentUserId!:number;
  @Input() currentUserProfilePicture:string='';
  activites: Activite[] = [];
  selectedActivite?: Activite;
  showActiviteForm = false;
  editingActivite?: Activite;
  activiteForm: any = {
    designation: '',
    description: '',
    dateDebut: new Date(),
    dateFin: new Date(),
    status: 'EN_ATTENTE',
    emailsString: ''
  };
  selectedFilesActivite: File[] = [];
  loading = false;

  constructor(
    private activiteService: ActiviteService,
    private authService: AuthService,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef,
    private fichierService:FichierService
  ) {}

  ngOnInit(): void {
    // Si currentUserId n'est pas fourni en @Input, on le récupère ici
   this.loadActivites(); 
  
  }

  loadActivites(): void {
    this.loading = true;
    this.activiteService.getByProjet(this.projetId).subscribe({
      next: (data) => {
        this.activites = data;
        if (this.activites.length > 0 && !this.selectedActivite) {
          this.selectedActivite = this.activites[0];
          this.activiteSelected.emit(this.selectedActivite);
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

  selectActivite(activite: Activite): void {
    this.selectedActivite = activite;
    console.log("Activité selectionnée")
    console.log(this.selectedActivite)
     this.activiteSelected.emit(this.selectedActivite);
  }

  onCreateActivite(): void {
    this.showActiviteForm = true;
    this.editingActivite = undefined;
    this.activiteForm = {
      designation: '',
      description: '',
      dateDebut: new Date(),
      dateFin: new Date(),
      status: 'EN_ATTENTE',
      emailsString: ''
    };
    this.selectedFilesActivite = [];
  }

  onEditActivite(activite: Activite): void {
    this.showActiviteForm = true;
    this.editingActivite = activite;
    this.activiteForm = {
      designation: activite.designation,
      description: activite.description || '',
      dateDebut: this.formatDateForInput(new Date(activite.dateDebut)),
      dateFin: this.formatDateForInput(new Date(activite.dateFin)),
      status: activite.status,
      emailsString: activite.emails?.map(e => e.email).join(', ') || ''
    };
    this.selectedFilesActivite = [];
  }
       private formatDateForInput(date: Date | string | undefined): string {
        if (!date) return '';
        const d = new Date(date);
        // Retourne YYYY-MM-DD
        return d.toISOString().split('T')[0];
      }
   

  onDeleteActivite(activite: Activite): void {
    if (confirm(`Supprimer l'activité "${activite.designation}" ?`)) {
      this.activiteService.delete(activite.id).subscribe({
        next: () => {
          this.activites = this.activites.filter(a => a.id !== activite.id);
          if (this.selectedActivite?.id === activite.id) {
            this.selectedActivite = this.activites.length > 0 ? this.activites[0] : undefined;
          }
          this.notification.success('Activité supprimée');
          this.activiteChanged.emit();
            this.activiteSelectedDelete.emit(this.selectedActivite);
        },
        error: (err) => {
          console.error('Erreur suppression', err);
          this.notification.error('Erreur lors de la suppression');
        }
      });
    }
  }

  onSaveActivite(): void {
    const dto: any = {
      designation: this.activiteForm.designation,
      description: this.activiteForm.description || undefined,
      dateDebut: this.activiteForm.dateDebut,
      dateFin: this.activiteForm.dateFin,
      status: this.activiteForm.status,
      emails: this.parseEmails(this.activiteForm.emailsString),
      createurId:this.currentUserId
    };

    if (this.editingActivite) {
      // Mise à jour
      this.activiteService.update( this.editingActivite.id, dto).subscribe({
        next: (updated) => {
          const index = this.activites.findIndex(a => a.id === updated.id);
          if (index !== -1) this.activites[index] = updated;
          if (this.selectedActivite?.id === updated.id) this.selectedActivite = updated;
          this.cancelActiviteForm();
          this.notification.success('Activité mise à jour');
          this.activiteChanged.emit();
        },
        error: (err) => {
          console.error('Erreur mise à jour', err);
          this.notification.error('Erreur lors de la mise à jour');
        }
      });
    } else {
      // Création
      dto.createurId = this.currentUserId;
      this.activiteService.create(this.projetId, dto, this.selectedFilesActivite).subscribe({
        next: (created) => {
          this.activites.push(created);
          this.selectedActivite = created;
          this.cancelActiviteForm();
          this.notification.success('Activité créée');
          this.activiteChanged.emit();
        },
        error: (err) => {
          console.error('Erreur création', err);
          this.notification.error('Erreur lors de la création');
        }
      });
    }
  }

  cancelActiviteForm(): void {
    this.showActiviteForm = false;
    this.editingActivite = undefined;
    this.activiteForm = {};
    this.selectedFilesActivite = [];
  }

  parseEmails(emailsString: string): string[] {
    return emailsString.split(',').map(email => email.trim()).filter(e => e.length > 0);
  }

  onFilesSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.selectedFilesActivite = Array.from(files);
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


  onApplieELementChange(editableChamps: EditableActivieDto) {
  let observable;
  let messageSuccess = '';
  let messageError = '';

  switch (editableChamps.type) {
    case 'DESIGNATION':
      observable = this.activiteService.updateActiviteDesignation(
        editableChamps.activite.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Désignation mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la désignation.';
      break;

    case 'DESCRIPTION':
      observable = this.activiteService.updateActiviteDescription(
        editableChamps.activite.id,
        { texte: editableChamps.texte }
      );
      messageSuccess = 'Description mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la description.';
      break;

    case 'DATE_DEBUT':
      observable = this.activiteService.updateActiviteDateDebut(
        editableChamps.activite.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de début mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de début.';
      break;

    case 'DATE_FIN':
      observable = this.activiteService.updateActiviteDateFin(
        editableChamps.activite.id,
        { date: editableChamps.date || new Date() }
      );
      messageSuccess = 'Date de fin mise à jour avec succès !';
      messageError = 'Erreur lors de la mise à jour de la date de fin.';
      break;

    default:
      return;
  }

  observable.subscribe({
    next: (activiteMisAJour) => {
     
    this.selectedActivite=activiteMisAJour;
      this.notification.success(messageSuccess)

    },
    error: (err) => {
      console.error(err);
     
      this.notification.error(messageError)
    }
  });

}

  updateActivite(liste: Activite[], activiteMisAJour: Activite) {
  const index = liste.findIndex(p => p.id === activiteMisAJour.id);
  if (index !== -1) {
    liste[index] = activiteMisAJour;
  }
}
// Remplacer onAddFiles par cette version
onAddFiles( files: File[],activite: Activite): void {
  this.fichierService.uploadFiles('ACTIVITE', activite.id, files).pipe(
    switchMap(() => this.fichierService.listFiles('ACTIVITE', activite.id))
  ).subscribe({
    next: (fichiers) => {
      const updatedFichiers: FichierInfo[] = fichiers.map(f => ({
        id: f.id,
        nomFichier: f.nomFichier ?? '',
        url: f.url ?? '',
        type: f.type ?? '',
        callbackurl:f.callbackurl??''
      }));

const updateActivite = (list: Activite[]) => {
  const idx = list.findIndex(p => p.id === activite.id);
  if (idx !== -1) {
    // Object.assign préserve l'instance de classe (et donc le getter simpleEmails)
    list[idx] = Object.assign({}, list[idx], { fichiers: updatedFichiers });
    this.selectedActivite=list[idx];
      this.activiteSelected.emit(this.selectedActivite);
  }
};

      updateActivite(this.activites);
      this.activiteChanged.emit()
      
      this.cdr.detectChanges();
    },
    error: (err) => {
      console.error('Erreur upload/récupération fichiers:', err);
      this.notification.error("Erreur lors de l'upload");
    }
  });
}

}
