import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OnlyofficeViewer } from './onlyoffice-viewer';

describe('OnlyofficeViewer', () => {
  let component: OnlyofficeViewer;
  let fixture: ComponentFixture<OnlyofficeViewer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OnlyofficeViewer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OnlyofficeViewer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
