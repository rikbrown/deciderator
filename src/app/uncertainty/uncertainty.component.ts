import {Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-uncertainty',
  templateUrl: './uncertainty.component.html',
  styleUrls: ['./uncertainty.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class UncertaintyComponent implements OnInit {
  onlineUsers = ['Rik', 'Mark'];

  uncertainty = {
    id: 'WHD',
    name: 'What game should we play next?',
    rules: {
      roundVictory: {
        bestOf: 5,
      },
      finalTwo: true
    },
    options: [
      {
        name: 'EU4',
      },
      {
        name: 'Civ VI',
        active: true,
      },
      {
        name: 'HOI4',
        eliminated: true,
      },
      {
        name: 'Stellaris',
      },
      {
        name: 'LOTRO',
      },
    ],
    activeOption: {
      name: 'Civ VI',
      results: {
        heads: [
          {
            flippedBy: 'Rik',
            waitTime: 7001,
            flipTime: 12576,
          },
          {
            flippedBy: 'Mark',
            waitTime: 1234,
            flipTime: 17653,
          },
          {
            flippedBy: 'Mark',
            waitTime: 32452,
            flipTime: 12345,
          }
        ],
        tails: [
          {
            flippedBy: 'Rik',
            waitTime: 23321,
            flipTime: 17532,
          },
          {
            flippedBy: 'Mark',
            waitTime: 2345,
            flipTime: 32443,
          },
        ]
      }
    },
  };

  constructor() { }

  ngOnInit(): void {
  }

}

export interface FlipResult {
  flippedBy: string;
  waitTime: number;
  flipTime: number;
}
