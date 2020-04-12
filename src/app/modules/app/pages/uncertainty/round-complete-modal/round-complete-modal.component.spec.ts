import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RoundCompleteModalComponent } from './round-complete-modal.component';

describe('FlippingEndModalComponent', () => {
  let component: RoundCompleteModalComponent;
  let fixture: ComponentFixture<RoundCompleteModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RoundCompleteModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RoundCompleteModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
