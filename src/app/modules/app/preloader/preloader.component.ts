import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-preloader',
  templateUrl: './preloader.component.html',
  styleUrls: ['./preloader.component.scss']
})
export class PreloaderComponent implements OnInit {
  ROUND_BACKGROUNDS = ['EU4', 'Civ', 'Stellaris', 'HoI', 'WoW']

  constructor() { }

  ngOnInit(): void {
  }

}
