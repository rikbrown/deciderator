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
  @Input() round: Round;
  // @Output() closed = new EventEmitter<void>();

  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private modalRef?: NgbModalRef = null;

  constructor(
    private modalService: NgbModal,
    private uncertaintyService: UncertaintyService) {}

  ngAfterViewInit(): void {
    this.onUpdate();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.onUpdate(changes.round, changes.option)
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

  private onUpdate(roundChange?: SimpleChange, optionChange?: SimpleChange) {
    console.log("onUpdate", roundChange, optionChange);

    const beforeRound: Round = roundChange?.previousValue;
    const beforeOption: UncertaintyOption = optionChange?.previousValue;

    if ((!beforeRound?.winningFace || beforeOption?.name !== this.option.name) && this.round.winningFace) {
      console.log('open');
      this.open();
    } else if (beforeRound?.winningFace && !this.round.winningFace) {
      console.log('close');
      this.modalRef?.close(true); // doesn't trigger (closed)
    }
  }


}
