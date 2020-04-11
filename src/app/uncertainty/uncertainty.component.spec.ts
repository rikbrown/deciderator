import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UncertaintyComponent } from './uncertainty.component';

describe('UncertaintyComponent', () => {
  let component: UncertaintyComponent;
  let fixture: ComponentFixture<UncertaintyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UncertaintyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UncertaintyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
