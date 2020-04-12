import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UncertaintyComponent } from './pages/uncertainty/uncertainty.component';
import { CoinComponent } from '../../coin/coin.component';
import { HumanizeDurationPipe } from '../../core/pipes/humanize-duration.pipe';
import { ResultsColComponent } from './pages/uncertainty/results-col/results-col.component';
import { RoundCompleteModalComponent } from './pages/uncertainty/round-complete-modal/round-complete-modal.component';
import {UncertaintyMockService, UncertaintyService} from '../../core/services/uncertainty/uncertainty.service';
import {CoinMockService, CoinService} from '../../core/services/coin/coin.service';

@NgModule({
  declarations: [
    AppComponent,
    UncertaintyComponent,
    CoinComponent,
    HumanizeDurationPipe,
    HumanizeDurationPipe,
    ResultsColComponent,
    RoundCompleteModalComponent,
  ],
  imports: [
    BrowserModule,

    NgbModule,

    AppRoutingModule,
  ],
  providers: [
    { provide: UncertaintyService, useClass: UncertaintyMockService },
    { provide: CoinService, useClass: CoinMockService },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
