import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared.module';
import {RouterModule} from '@angular/router';
import {HomeComponent} from './home.component';
import {JoinComponent} from './join/join.component';
import {CreateComponent} from './create/create.component';
import {FormsModule} from '@angular/forms';
import {UncertaintyModule} from "../uncertainty/uncertainty.module";

@NgModule({
  declarations: [
    HomeComponent,
    JoinComponent,
    CreateComponent,
  ],
    imports: [
        SharedModule,
        RouterModule,
        FormsModule,
        UncertaintyModule,
    ]
})

export class HomeModule { }
