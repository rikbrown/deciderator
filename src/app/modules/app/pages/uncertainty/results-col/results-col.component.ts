import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-results-col',
  templateUrl: './results-col.component.html',
  styleUrls: ['./results-col.component.scss'],
})
export class ResultsColComponent implements OnInit {
  @Input() results: FlipResult[];
  @Input() face: string;

  constructor() { }

  ngOnInit(): void {
  }

}
