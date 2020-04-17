import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {UncertaintyMockService, UncertaintyService} from '../../core/services/uncertainty/uncertainty.service';
import {CoinMockService, CoinService} from '../../core/services/coin/coin.service';
import { HomeComponent } from './pages/home/home.component';
import {UncertaintyModule} from './pages/uncertainty/uncertainty.module';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../shared.module';
import { JoinComponent } from './pages/home/join/join.component';
import {FormsModule} from '@angular/forms';
import { CreateComponent } from './pages/home/create/create.component';
import {HomeModule} from './pages/home/home.module';

@NgModule({
  declarations: [
    AppComponent,
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
    { provide: UncertaintyService, useClass: UncertaintyMockService },
    { provide: CoinService, useClass: CoinMockService },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
