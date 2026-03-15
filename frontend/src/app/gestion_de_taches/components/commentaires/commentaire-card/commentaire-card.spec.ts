import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentaireCard } from './commentaire-card';

describe('CommentaireCard', () => {
  let component: CommentaireCard;
  let fixture: ComponentFixture<CommentaireCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommentaireCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommentaireCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
