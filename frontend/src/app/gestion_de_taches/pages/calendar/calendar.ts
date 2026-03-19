// calendar.component.ts
import { Component, inject, OnInit, OnDestroy, AfterViewInit, signal, ViewChild, ElementRef } from '@angular/core';
import { FullCalendarModule, FullCalendarComponent } from '@fullcalendar/angular';
import { CalendarOptions, EventClickArg, EventMountArg } from '@fullcalendar/core';
import { FormsModule } from '@angular/forms';

import dayGridPlugin     from '@fullcalendar/daygrid';
import timeGridPlugin    from '@fullcalendar/timegrid';
import listPlugin        from '@fullcalendar/list';
import interactionPlugin from '@fullcalendar/interaction';
import frLocale          from '@fullcalendar/core/locales/fr';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { CalendarEventDto, CalendarService } from '../../services/calendar.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

type EntityFilter = 'TOUS' | 'PROJET' | 'ACTIVITE' | 'TACHE';

@Component({
  selector:    'app-calendar',
  standalone:  true,
  templateUrl: './calendar.html',
  styleUrls:   ['./calendar.css'],
  imports:     [FullCalendarModule, FormsModule, CommonModule]
})
export class Calendar implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('fullcalendar')    fullCalendarRef!: FullCalendarComponent;
  @ViewChild('calendarWrapper') calendarWrapper!: ElementRef<HTMLElement>;

  private calendarService = inject(CalendarService);
  private router          = inject(Router);
  private breakpoint      = inject(BreakpointObserver);

  private resizeObserver!: ResizeObserver;
  private subs = new Subscription();

  // ── Signaux ──────────────────────────────────────────────────
  activeFilter = signal<EntityFilter>('TOUS');
  isMobileView = signal<boolean>(false);

  // ── Filtres ──────────────────────────────────────────────────
  readonly filters: { value: EntityFilter; label: string; icon: string }[] = [
    { value: 'TOUS',     label: 'Tout',      icon: '🗂️' },
    { value: 'PROJET',   label: 'Projets',   icon: '📁' },
    { value: 'ACTIVITE', label: 'Activités', icon: '📋' },
    { value: 'TACHE',    label: 'Tâches',    icon: '✅' },
  ];

  private allEvents: CalendarEventDto[] = [];

  // ── Options FullCalendar ─────────────────────────────────────
  calendarOptions: CalendarOptions = {
    plugins:     [dayGridPlugin, timeGridPlugin, listPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    locale:      frLocale,
    height: 'auto',
    
    headerToolbar: {
      left:   'prev,next today',
      center: 'title',
      right:  'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
    },

    buttonText: {
      today: "Aujourd'hui",
      month: 'Mois',
      week:  'Semaine',
      day:   'Jour',
      list:  'Agenda'
    },

    events: (info, successCallback, failureCallback) => {
      const start = info.startStr.substring(0, 10);
      const end   = info.endStr.substring(0, 10);

      this.calendarService.getEvents(start, end).subscribe({
        next: events => {
          this.allEvents = events;
          successCallback(this.applyFilter(events));
        },
        error: err => {
          console.error('Erreur chargement calendrier', err);
          failureCallback(err);
        }
      });
    },

    eventClick:    this.onEventClick.bind(this),
    eventDidMount: this.onEventDidMount.bind(this),
    
    // Options pour mobile
    dayMaxEvents: 3,
    eventMaxStack: 3,
    moreLinkText: (n) => `+${n}`,
  };

  // ── ngOnInit — responsive ────────────────────────────────────
  ngOnInit(): void {
    const sub = this.breakpoint
      .observe([Breakpoints.HandsetPortrait, Breakpoints.HandsetLandscape, Breakpoints.TabletPortrait])
      .subscribe(result => {
        const isMobile = result.matches;
        const isSmallMobile = window.innerWidth <= 380;
        
        this.isMobileView.set(isMobile);
        
        // Configuration responsive
        this.calendarOptions = {
          ...this.calendarOptions,
          initialView: this.getInitialView(isMobile),
          height: 'auto',
          headerToolbar: this.getResponsiveToolbar(isMobile, isSmallMobile),
          buttonText: this.getResponsiveButtonText(isMobile),
          dayMaxEvents: isMobile ? 2 : 3,
          eventMaxStack: isMobile ? 2 : 3,
        };
        
        // Forcer la mise à jour du calendrier si déjà chargé
        if (this.fullCalendarRef) {
          setTimeout(() => {
            const calendarApi = this.fullCalendarRef.getApi();
            if (calendarApi) {
              calendarApi.changeView(this.calendarOptions.initialView!);
              calendarApi.updateSize();
            }
          }, 100);
        }
      });

    this.subs.add(sub);
  }

  // ── Méthodes helper pour le responsive ───────────────────────
  private getInitialView(isMobile: boolean): string {
    // Sur mobile, on préfère la vue liste ou mois selon la place
    return isMobile ? 'listWeek' : 'dayGridMonth';
  }

  private getResponsiveToolbar(isMobile: boolean, isSmallMobile: boolean): any {
    if (isSmallMobile) {
      return {
        left: 'prev,next',
        center: 'title',
        right: 'today'
      };
    }
    
    if (isMobile) {
      return {
        left: 'prev,next',
        center: 'title',
        right: 'listWeek,dayGridMonth'
      };
    }
    
    return {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
    };
  }

  private getResponsiveButtonText(isMobile: boolean): any {
    if (isMobile) {
      return {
        today: 'Auj',
        list: 'Agenda',
        month: 'Mois'
      };
    }
    
    return {
      today: "Aujourd'hui",
      month: 'Mois',
      week: 'Semaine',
      day: 'Jour',
      list: 'Agenda'
    };
  }

  // ── ngAfterViewInit — ResizeObserver ─────────────────────────
  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.fullCalendarRef) {
        const calendarApi = this.fullCalendarRef.getApi();

        this.resizeObserver = new ResizeObserver(() => {
          calendarApi.updateSize();
        });

        this.resizeObserver.observe(this.calendarWrapper.nativeElement);
      }
    }, 500);
  }

  // ── ngOnDestroy ──────────────────────────────────────────────
  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.subs.unsubscribe();
  }

  // ── Filtres ──────────────────────────────────────────────────
  setFilter(filter: EntityFilter): void {
    this.activeFilter.set(filter);
    
    if (this.fullCalendarRef) {
      const calendarApi = this.fullCalendarRef.getApi();
      calendarApi.removeAllEvents();
      calendarApi.addEventSource(this.applyFilter(this.allEvents));
    }
  }

  private applyFilter(events: CalendarEventDto[]): CalendarEventDto[] {
    const f = this.activeFilter();
    if (f === 'TOUS') return events;
    return events.filter(e => e.extendedProps?.['type'] === f);
  }

  // ── Click événement ──────────────────────────────────────────
  onEventClick(info: EventClickArg): void {
    const { type, id } = info.event.extendedProps as any;
    const label = info.event.title;

    // Confirmation plus adaptée au mobile
    if (window.innerWidth <= 640) {
      // Sur mobile, pas de confirm pour éviter les problèmes d'affichage
      this.navigateToEntity(type, id);
    } else {
      const confirmed = confirm(`Voir le détail de :\n"${label}" ?`);
      if (!confirmed) return;
      this.navigateToEntity(type, id);
    }
  }

  private navigateToEntity(type: string, id: number): void {
    switch (type) {
      case 'PROJET':
        this.router.navigate(['/app/projets', id]);
        break;
      case 'ACTIVITE':
        this.router.navigate(['/app/activites', id]);
        break;
      case 'TACHE':
        this.router.navigate(['/app/taches', id]);
        break;
    }
  }

  // ── Styles événements ────────────────────────────────────────
  onEventDidMount(info: EventMountArg): void {
    const { status, type } = info.event.extendedProps as any;

    info.el.setAttribute('title', `${type} | Statut : ${status}`);

    const colors: Record<string, string> = {
      PROJET:   '#3B82F6',
      ACTIVITE: '#10B981',
      TACHE:    '#F59E0B',
    };
    
    if (colors[type]) {
      info.el.style.backgroundColor = colors[type];
    }

    if (status === 'TERMINE' || status === 'ANNULE') {
      info.el.style.opacity = '0.5';
    }
    
    // Ajustement pour mobile
    if (window.innerWidth <= 640) {
      info.el.style.padding = '1px 2px';
      info.el.style.margin = '0';
      info.el.style.fontSize = '0.6rem';
    }
  }
}