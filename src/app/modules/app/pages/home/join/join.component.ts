import {Component, EventEmitter, OnInit, Output, TemplateRef, ViewChild, ElementRef, ViewChildren} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyService} from '../../../../../core/services/uncertainty/uncertainty.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-join',
  templateUrl: './join.component.html',
  styleUrls: ['./join.component.scss']
})
export class JoinComponent {
  @ViewChild('content') private contentTmpl: TemplateRef<any>;

  uncertaintyId: string
  submitted: boolean = false;
  failure?: string = null;
  private modalRef: NgbModalRef;

  constructor(
    private uncertaintyService: UncertaintyService,
    private modalService: NgbModal,
    private router: Router,
  ) { }

  open(): void {
    this.modalRef = this.modalService.open(this.contentTmpl, {
      centered: true,
    });
  }

  onSubmit() {
    this.submitted = true;
    this.failure = null;
    this.uncertaintyService.getUncertainty(this.uncertaintyId).subscribe(
      () => {
        this.router.navigate(['/uncertainty', this.uncertaintyId]).then(() => this.modalRef?.close());
      },
      (err) => {
        this.uncertaintyId = null;
        this.submitted = false;
        this.failure = err.toString();
      });
  }
}
