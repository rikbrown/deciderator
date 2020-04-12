import {
  AfterViewInit,
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

@Component({
  selector: 'app-uncertainty',
  templateUrl: './uncertainty.component.html',
  styleUrls: ['./uncertainty.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class UncertaintyComponent implements OnInit, AfterViewInit, OnChanges {
  onlineUsers = ['Rik', 'Mark'];
  availableCoinStyles = ['germany', 'eu_germany', 'usa'];
  coinState = null;
  @Input() uncertainty: Uncertainty = null;
  @ViewChild(RoundCompleteModalComponent) private roundCompleteModal: RoundCompleteModalComponent;
  @ViewChild(CoinComponent) private coinComponent: CoinComponent;

  constructor(private coinService: CoinService) {}

  get activeOption(): UncertaintyOption {
    return this.uncertainty.options.find(it => it.active);
  }

  ngOnInit(): void {
    this.coinService.observeCoinState('foo').subscribe(coinState => this.coinState = coinState);
    // this.service.getUncertainty('foo').subscribe(this.updateUncertainty);
  }

  ngAfterViewInit(): void {
    this.onUncertaintyUpdate();
    this.coinComponent.observeCoinState().subscribe(it => this.coinService.updateCoinState('foo', it));
  }

  ngOnChanges(changes: SimpleChanges): void {
    for (const propName of Object.keys(changes)) {
      // noinspection JSRedundantSwitchStatement
      switch (propName) {
        case 'uncertainty':
          this.onUncertaintyUpdate();
      }
    }
  }

  changeCoinStyle(event: Event): void {
    event.preventDefault();

    const nextCoinIndex = (this.availableCoinStyles.indexOf(this.activeOption.active.coinStyle) + 1) % this.availableCoinStyles.length;
    this.activeOption.active.coinStyle = this.availableCoinStyles[nextCoinIndex];
  }

  private onUncertaintyUpdate(): void {
    if (this.activeOption.active.roundComplete) {
      this.roundCompleteModal?.open();
    }
  }


  // private updateUncertainty(uncertainty: Uncertainty): void {
  //   this.uncertainty = uncertainty;
  //   console.log(this.uncertainty);
  //
  //   this.cdRef.detectChanges();
  // }

}


