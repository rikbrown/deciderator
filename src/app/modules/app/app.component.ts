import {Component, OnInit} from '@angular/core';
import {UncertaintyService} from '../../core/services/uncertainty/uncertainty.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'Deciderator 3.0';
  uncertainty = null;

  constructor(
    private service: UncertaintyService
  ) {}

  ngOnInit() {
    this.service.getUncertainty('foo').subscribe(uncertainty => {
      this.uncertainty = uncertainty;
      console.log(this.uncertainty);
    });
  }

}
