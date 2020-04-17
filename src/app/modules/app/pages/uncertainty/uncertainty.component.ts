import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {RoundCompleteModalComponent} from './round-complete-modal/round-complete-modal.component';
import {Uncertainty, UncertaintyOption} from '../../../../core/services/uncertainty/types';
import {CoinService} from '../../../../core/services/coin/coin.service';
import {CoinComponent} from '../../../../coin/coin.component';
import {UncertaintyService} from '../../../../core/services/uncertainty/uncertainty.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-uncertainty',
  template: '<app-uncertainty-inner [uncertainty]="uncertainty"></app-uncertainty-inner>'
})
export class UncertaintyComponent implements OnInit {
  @Input() uncertaintyId: string;
  uncertainty: Uncertainty = null;

  constructor(private service: UncertaintyService) {}

  ngOnInit() {
    this.service.getUncertainty('foo').subscribe(uncertainty => {
      this.uncertainty = uncertainty;
      console.log(this.uncertainty);
    });
  }
}

@Component({
  selector: 'app-uncertainty-inner',
  templateUrl: './uncertainty.component.html',
  styleUrls: ['./uncertainty.component.scss'],
  // encapsulation: ViewEncapsulation.None
})
export class UncertaintyInnerComponent implements OnInit, AfterViewInit, OnChanges {
  onlineUsers = ['Rik', 'Mark'];
  availableCoinStyles = ['germany', 'eu_germany', 'usa'];
  coinState = null;

  @Input() uncertainty: Uncertainty = null;

  @ViewChild(RoundCompleteModalComponent) private roundCompleteModal: RoundCompleteModalComponent;
  @ViewChild(CoinComponent) private coinComponent: CoinComponent;

  constructor(
    private coinService: CoinService,
    private uncertaintyService: UncertaintyService) {}

  get activeOption(): UncertaintyOption {
    return this.uncertainty.options.find(it => it.active);
  }

  ngOnInit(): void {
    this.coinService.observeCoinState('foo').subscribe(coinState => this.coinState = coinState);
  }

  ngAfterViewInit(): void {
    this.coinComponent.observeCoinState().subscribe(it => this.coinService.updateCoinState('foo', it));
  }

  ngOnChanges(changes: SimpleChanges): void {
    // for (const [propName, change] of Object.entries(changes)) {
    //   // noinspection JSRedundantSwitchStatement
    //   switch (propName) {
    //     case 'uncertainty':
    //       // this.onUncertaintyUpdate();
    //   }
    // }
  }

  changeCoinStyle(event: Event): void {
    event.preventDefault();

    const nextCoinIndex = (this.availableCoinStyles.indexOf(this.activeOption.active.coinStyle) + 1) % this.availableCoinStyles.length;
    this.activeOption.active.coinStyle = this.availableCoinStyles[nextCoinIndex];
  }
}

