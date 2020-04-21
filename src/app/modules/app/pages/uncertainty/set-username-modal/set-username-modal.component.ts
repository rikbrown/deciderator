import {Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyService} from '../../../../../core/services/uncertainty/uncertainty.service';
import {Router} from '@angular/router';
import {untilComponentDestroyed} from '@w11k/ngx-componentdestroyed';
import {first} from 'rxjs/operators';
import {UsernameService} from '../../../../../core/services/username/username.service';

@Component({
  selector: 'app-set-username-modal',
  templateUrl: './set-username-modal.component.html',
  styleUrls: ['./set-username-modal.component.scss']
})
export class SetUsernameModalComponent {
  username: string = null;
  @ViewChild('content') private contentTmpl: TemplateRef<any>;
  private modalRef: NgbModalRef;

  constructor(
    private usernameService: UsernameService,
    private modalService: NgbModal,
  ) { }

  openIfNeeded(): void {
    if (this.usernameService.getUsernameFromStorage() == null) {
      this.open();
    }
  }

  open(): void {
    this.username = this.usernameService.getUsernameFromStorage();
    this.modalRef = this.modalService.open(this.contentTmpl, {
      beforeDismiss: () => false,
      centered: true,
    });
  }

  onSubmit() {
    this.usernameService.setUsername(this.username);
    this.modalRef.close();
  }
}
