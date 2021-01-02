import { Component, OnInit } from '@angular/core';
import {AVAILABLE_COIN_STYLES} from "../../../coin/constants";

@Component({
  selector: 'app-preloader',
  templateUrl: './preloader.component.html',
  styleUrls: ['./preloader.component.scss']
})
export class PreloaderComponent implements OnInit {
  ROUND_BACKGROUNDS = ['EU4', 'Civ', 'Stellaris', 'HoI', 'WoW', 'SoT', 'ESO']
  COIN_ASSETS = AVAILABLE_COIN_STYLES.flatMap(c => [
    'assets/img/coins/' + c + '/heads.png',
    'assets/img/coins/' + c + '/tails.png',
    'assets/img/coins/' + c + '/edge.png',
  ])

  constructor() { }

  ngOnInit(): void {
  }

}
