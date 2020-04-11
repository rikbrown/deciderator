import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {FlipResult} from '../uncertainty.component';

@Component({
  selector: 'app-results-col',
  templateUrl: './results-col.component.html',
  styleUrls: ['./results-col.component.scss'],
})
export class ResultsColComponent implements OnInit {
  @Input() title: string;
  @Input() results: FlipResult[];

  constructor() { }

  ngOnInit(): void {
  }

}
