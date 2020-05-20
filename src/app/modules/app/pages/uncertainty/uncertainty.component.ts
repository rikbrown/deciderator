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
import {SetUsernameModalComponent} from './set-username-modal/set-username-modal.component';
import {UsernameService} from '../../../../core/services/username/username.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-uncertainty',
  template: '<app-uncertainty-inner [uncertainty]="uncertainty" [onlineUsers]="onlineUsers"></app-uncertainty-inner>'
})
export class UncertaintyComponent extends OnDestroyMixin implements OnInit, OnDestroy {
  uncertainty: Uncertainty = null;
  onlineUsers: UncertaintyUser[] = [];

  constructor(
    private route: ActivatedRoute,
    private service: UncertaintyService,
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
  availableCoinStyles = ['germany', 'eu_germany', 'usa', 'usa_trump', 'japan', 'ducat'];
  coinState: CoinState = null;

  @Input() uncertainty: Uncertainty = null;
  @Input() onlineUsers: UncertaintyUser[] = [];
  @ViewChild(SetUsernameModalComponent) setUsernameModal: SetUsernameModalComponent;

  @ViewChild(RoundCompleteModalComponent) private roundCompleteModal: RoundCompleteModalComponent;
  @ViewChild(CoinComponent) private coinComponent: CoinComponent;

  private coinStateSubscription: Subscription = null;

  constructor(
    private coinService: CoinService) {
    super();
  }

  get activeOption(): [UncertaintyOption, UncertaintyOption?] {
    const roundData = this.uncertainty?.currentRound.data
    if (isHeadToHead(roundData)) {
      return [
        this.uncertainty?.options.find(it => it.name == roundData.headsOption),
        this.uncertainty?.options.find(it => it.name == roundData.tailsOption),
      ]
    } else if (isMeaningfulVote(roundData)) {
      return [this.uncertainty?.options.find(it => it.name == roundData.option), null]
    }
  }

  get headsResults(): FlipResult[] {
    return this.uncertainty?.currentRound.results.filter(it => it.result === 'HEADS');
  }

  get tailsResults(): FlipResult[] {
    return this.uncertainty?.currentRound.results.filter(it => it.result === 'TAILS');
  }

  isActive(optionName: string): boolean {
    return !!this.activeOption?.find(it => it?.name == optionName);
  }

  isHeadToHead(): boolean {
    return isHeadToHead(this.uncertainty?.currentRound.data)
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.setUsernameModal.openIfNeeded();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.uncertainty) {
      this.onUncertaintyUpdate();
    }
  }

  changeCoinStyle(event: Event): void {
    event.preventDefault();

    if (!this.coinState.interactive) {
      return;
    }

    const nextCoinIndex = (this.availableCoinStyles.indexOf(this.uncertainty.currentRound.coinStyle) + 1) % this.availableCoinStyles.length;
    this.uncertainty.currentRound.coinStyle = this.availableCoinStyles[nextCoinIndex];
  }

  private onUncertaintyUpdate() {
    this.coinStateSubscription?.unsubscribe();
    if (this.uncertainty?.id) {
      this.coinStateSubscription = this.coinService.observeCoinState(this.uncertainty.id)
        .pipe(untilComponentDestroyed(this))
        .subscribe(coinState => this.coinState = coinState);
    }
  }
}

function isHeadToHead(x: RoundData): x is HeadToHeadRound {
  return 'headsOption' in x
}

function isMeaningfulVote(x: RoundData): x is MeaningfulVoteRound {
  return 'option' in x
}


