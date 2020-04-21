import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges, OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {RoundCompleteModalComponent} from './round-complete-modal/round-complete-modal.component';
import {CoinService} from '../../../../core/services/coin/coin.service';
import {CoinComponent} from '../../../../coin/coin.component';
import {UncertaintyService, UncertaintyUser} from '../../../../core/services/uncertainty/uncertainty.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {OnDestroyMixin, untilComponentDestroyed} from '@w11k/ngx-componentdestroyed';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-uncertainty',
  template: '<app-uncertainty-inner [uncertainty]="uncertainty" [onlineUsers]="onlineUsers"></app-uncertainty-inner>'
})
export class UncertaintyComponent extends OnDestroyMixin implements OnInit, OnDestroy {
  uncertainty: Uncertainty = null;
  onlineUsers: UncertaintyUser[] = [];

  constructor(
    private route: ActivatedRoute,
    private service: UncertaintyService
  ) {
    super();
  }

  ngOnInit() {
    const uncertaintyId = this.route.snapshot.paramMap.get('uncertaintyId');

    this.service.observeUncertaintyUsers(uncertaintyId)
      .pipe(untilComponentDestroyed(this))
      .subscribe(users => this.onlineUsers = users);

    this.service.joinUncertainty(uncertaintyId)
      .subscribe(uncertainty => {
        this.uncertainty = uncertainty;
      });

    this.service.observeUncertainty(uncertaintyId)
      .pipe(untilComponentDestroyed(this))
      .subscribe(uncertainty => {
        this.uncertainty = uncertainty;
      });
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (this.uncertainty != null) {
      this.service.leaveUncertainty(this.uncertainty.id);
    }
  }
}

@Component({
  selector: 'app-uncertainty-inner',
  templateUrl: './uncertainty.component.html',
  styleUrls: ['./uncertainty.component.scss'],
  // encapsulation: ViewEncapsulation.None
})
export class UncertaintyInnerComponent extends OnDestroyMixin implements OnInit, AfterViewInit, OnChanges {
  availableCoinStyles = ['germany', 'eu_germany', 'usa', 'usa_trump'];
  coinState = null;

  @Input() uncertainty: Uncertainty = null;
  @Input() onlineUsers: UncertaintyUser[] = [];

  @ViewChild(RoundCompleteModalComponent) private roundCompleteModal: RoundCompleteModalComponent;
  @ViewChild(CoinComponent) private coinComponent: CoinComponent;

  constructor(
    private coinService: CoinService,
    private uncertaintyService: UncertaintyService) {
    super();
  }

  get activeOption(): UncertaintyOption {
    return this.uncertainty?.options.find(it => it.active);
  }

  get activeTails(): FlipResult[] {
    return this.activeOption.active.results.filter(it => it.result === 'TAILS');
  }

  get activeHeads(): FlipResult[] {
    return this.activeOption.active.results.filter(it => it.result === 'HEADS');
  }

  ngOnInit(): void {
    this.coinService.observeCoinState('foo')
      .pipe(untilComponentDestroyed(this))
      .subscribe(coinState => this.coinState = coinState);
  }

  ngAfterViewInit(): void {
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

