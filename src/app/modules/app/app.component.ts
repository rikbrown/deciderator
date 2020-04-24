import {Component, OnInit} from '@angular/core';
import {UncertaintyService} from '../../core/services/uncertainty/uncertainty.service';
import {DecideratorSocketService} from '../../core/services/deciderator-socket/deciderator-socket.service';
import {OnDestroyMixin, untilComponentDestroyed} from '@w11k/ngx-componentdestroyed';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Deciderator 3.0';

}
