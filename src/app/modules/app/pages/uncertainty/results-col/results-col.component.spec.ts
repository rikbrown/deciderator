import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsColComponent } from './results-col.component';

describe('ResultsColComponent', () => {
  let component: ResultsColComponent;
  let fixture: ComponentFixture<ResultsColComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultsColComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsColComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
