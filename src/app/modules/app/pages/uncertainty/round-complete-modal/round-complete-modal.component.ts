import {Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyOption} from '../../../../../core/services/uncertainty/types';

@Component({
  selector: 'app-round-complete-modal',
  templateUrl: './round-complete-modal.component.html',
  styleUrls: ['./round-complete-modal.component.scss']
})
export class RoundCompleteModalComponent implements OnInit {
  @Input() option: UncertaintyOption;
  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private isOpened = false;

  constructor(private modalService: NgbModal) {}

  ngOnInit(): void {
  }

  open(): void {
    if (this.isOpened) {
      return;
    }

    const modalRef = this.modalService.open(this.contentTmpl, {
      windowClass: this.option.eliminated ? 'eliminated' : null,
      centered: true,
    });
    this.isOpened = true;

    modalRef.result.finally(this.onClose);
  }

  private onClose(): void {
    console.log('on close');
  }

}
