  import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class CalendarService {

  private readonly api = inject(HttpClient);
    private readonly API_URL = environment.API_URL + '/tasksmanager/calendar';

  getEvents(start: string, end: string): Observable<CalendarEventDto[]> {
    return this.api.get<CalendarEventDto[]>(this.API_URL, {
      params: { start, end }
    });
  }
}

// calendar-event.dto.ts
export interface CalendarEventDto {
  id:            string;
  title:         string;
  start:         string;
  end:           string;
  color:         string;
  textColor:     string;
  allDay:        boolean;
  extendedProps: {
    type:        'PROJET' | 'ACTIVITE' | 'TACHE';
    status:      'EN_COURS' | 'TERMINE' | 'EN_ATTENTE' | 'ANNULE';
    projetId:    number | null;
    activiteId:  number | null;
  };
}
