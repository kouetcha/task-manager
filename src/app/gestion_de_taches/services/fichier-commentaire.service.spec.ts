import { TestBed } from '@angular/core/testing';

import { FichierCommentaireService } from './fichier-commentaire.service';

describe('FichierCommentaireService', () => {
  let service: FichierCommentaireService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FichierCommentaireService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
