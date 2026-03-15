import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FileListDialog } from './file-list-dialog';

describe('FileListDialog', () => {
  let component: FileListDialog;
  let fixture: ComponentFixture<FileListDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileListDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FileListDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
