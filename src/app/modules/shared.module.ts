import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HumanizeDurationPipe} from '../core/pipes/humanize-duration.pipe';

@NgModule({
  imports:      [
    CommonModule,
    NgbModule
  ],
  declarations: [
    HumanizeDurationPipe,
  ],
  exports: [
    CommonModule,
    NgbModule,
    HumanizeDurationPipe,
  ]
})
export class SharedModule { }
