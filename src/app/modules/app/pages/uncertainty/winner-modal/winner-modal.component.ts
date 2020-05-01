import {AfterViewInit, Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-winner-modal',
  templateUrl: './winner-modal.component.html',
  styleUrls: ['./winner-modal.component.scss']
})
export class WinnerModalComponent implements AfterViewInit {
  @Input() winner: Winner;
  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private modalRef: NgbModalRef;

  constructor(
    private modalService: NgbModal,
  ) { }

  ngAfterViewInit(): void {
    this.open();
  }

  open(): void {
    this.modalRef = this.modalService.open(this.contentTmpl, {
      beforeDismiss: () => false,
      centered: true,
    });
  }

}
