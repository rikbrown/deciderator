import {
  AfterViewInit,
  Component, EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyOption} from '../../../../../core/services/uncertainty/types';
import {NgbModalRef} from '@ng-bootstrap/ng-bootstrap/modal/modal-ref';
import {BehaviorSubject, Subject} from 'rxjs';
import {UncertaintyService} from '../../../../../core/services/uncertainty/uncertainty.service';

@Component({
  selector: 'app-round-complete-modal',
  templateUrl: './round-complete-modal.component.html',
  styleUrls: ['./round-complete-modal.component.scss'],
})
export class RoundCompleteModalComponent implements AfterViewInit, OnChanges {
  @Input() uncertaintyId: string;
  @Input() option: UncertaintyOption;
  // @Output() closed = new EventEmitter<void>();

  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private modalRef?: NgbModalRef = null;

  constructor(
    private modalService: NgbModal,
    private uncertaintyService: UncertaintyService) {}

  ngAfterViewInit(): void {
    this.onOptionUpdate();
  }

  ngOnChanges(changes: SimpleChanges): void {
    for (const [propName, change] of Object.entries(changes)) {
      if (propName === 'option') {
        if (change.previousValue != null) { // first onchange fires before view initialised, so ignore it - we'll call in ngAfterViewInit
          this.onOptionUpdate(change);
        }
      }
    }
  }

  open() {
    this.modalRef = this.modalService.open(this.contentTmpl, {
      centered: true,
    });

    this.modalRef.result.then(() => {
      this.modalRef = null;
      this.nextRound();
    }, () => {});
  }

  nextRound() {
    this.uncertaintyService.nextRound(this.uncertaintyId);
  }

  private onOptionUpdate(change?: SimpleChange) {
    const before: UncertaintyOption = change?.previousValue;
    if ((!before?.active?.roundComplete || before?.name !== this.option.name) && this.option.active?.roundComplete) {
      this.open();
    } else if (before?.active?.roundComplete && !this.option.active?.roundComplete) {
      this.modalRef?.dismiss(); // doesn't trigger (closed)
    }
  }


}
