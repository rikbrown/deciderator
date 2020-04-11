import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UncertaintyComponent } from './uncertainty/uncertainty.component';
import { CoinComponent } from './coin/coin.component';
import { HumanizeDurationPipe } from './humanize-duration.pipe';
import { ResultsColComponent } from './uncertainty/results-col/results-col.component';

@NgModule({
  declarations: [
    AppComponent,
    UncertaintyComponent,
    CoinComponent,
    HumanizeDurationPipe,
    HumanizeDurationPipe,
    ResultsColComponent
  ],
  imports: [
    BrowserModule,

    NgbModule,

    AppRoutingModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
