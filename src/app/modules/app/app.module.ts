import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {CoinService, DecideratorCoinService} from '../../core/services/coin/coin.service';
import {UncertaintyModule} from './pages/uncertainty/uncertainty.module';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../shared.module';
import {HomeModule} from './pages/home/home.module';
import {DecideratorUncertaintyService, UncertaintyService} from '../../core/services/uncertainty/uncertainty.service';
import { PreloaderComponent } from './preloader/preloader.component';

@NgModule({
  declarations: [
    AppComponent,
    PreloaderComponent,
  ],
  imports: [
    CommonModule,
    BrowserModule,

    AppRoutingModule,
    SharedModule,

    HomeModule,
    UncertaintyModule
  ],
  providers: [
    { provide: UncertaintyService, useClass: DecideratorUncertaintyService },
    { provide: CoinService, useClass: DecideratorCoinService },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
