import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentaireList } from './commentaire-list';

describe('CommentaireList', () => {
  let component: CommentaireList;
  let fixture: ComponentFixture<CommentaireList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommentaireList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommentaireList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
