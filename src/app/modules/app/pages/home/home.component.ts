import {AfterViewInit, Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyService} from '../../../../core/services/uncertainty/uncertainty.service';
import {Router} from '@angular/router';
import {RoundCompleteModalComponent} from '../uncertainty/round-complete-modal/round-complete-modal.component';
import {JoinComponent} from './join/join.component';
import {CreateComponent} from './create/create.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class HomeComponent implements AfterViewInit {
  @ViewChild(JoinComponent) joinModal: JoinComponent;
  @ViewChild(CreateComponent) createModal: CreateComponent;

  constructor(
  ) {}

  ngAfterViewInit(): void {
    this.createModal.open();
  }

}
