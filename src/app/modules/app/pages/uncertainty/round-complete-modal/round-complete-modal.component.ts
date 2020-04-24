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
    if (changes.option && changes.option.previousValue != null) {
      // check previous value because first onchange fires before view initialised, so ignore it - we'll call in ngAfterViewInit
      this.onOptionUpdate(changes.option);
    }
  }

  open() {
    this.modalRef = this.modalService.open(this.contentTmpl, {
      centered: true,
      beforeDismiss: () => false,
    });

    this.modalRef.result.then((noTrigger) => {
      if (!noTrigger) {
        this.modalRef = null;
        this.nextRound();
      }
    });
  }

  nextRound() {
    this.uncertaintyService.nextRound(this.uncertaintyId);
  }

  private onOptionUpdate(change?: SimpleChange) {
    const before: UncertaintyOption = change?.previousValue;
    if ((!before?.active?.roundComplete || before?.name !== this.option.name) && this.option?.active?.roundComplete) {
      this.open();
    } else if (before?.active?.roundComplete && !this.option?.active?.roundComplete) {
      this.modalRef?.close(true); // doesn't trigger (closed)
    }
  }


}
