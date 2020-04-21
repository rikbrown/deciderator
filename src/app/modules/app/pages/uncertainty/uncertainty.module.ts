import {NgModule} from '@angular/core';
import {CoinComponent} from '../../../../coin/coin.component';
import {ResultsColComponent} from './results-col/results-col.component';
import {RoundCompleteModalComponent} from './round-complete-modal/round-complete-modal.component';
import {UncertaintyComponent, UncertaintyInnerComponent} from './uncertainty.component';
import {HumanizeDurationPipe} from '../../../../core/pipes/humanize-duration.pipe';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../shared.module';
import {RouterModule} from '@angular/router';
import { SetUsernameModalComponent } from './set-username-modal/set-username-modal.component';
import {FormsModule} from '@angular/forms';

@NgModule({
  declarations: [
    UncertaintyComponent,
    UncertaintyInnerComponent,
    CoinComponent,
    ResultsColComponent,
    RoundCompleteModalComponent,
    SetUsernameModalComponent,
  ],
  imports: [
    SharedModule,
    RouterModule,
    FormsModule,
  ]
})

export class UncertaintyModule { }
