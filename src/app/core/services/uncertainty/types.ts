export interface Uncertainty {
  id: string;
  name: string;
  rules: UncertaintyRules;
  options: UncertaintyOption[];
}

export interface UncertaintyRules {
  bestOf: number;
  finalTwoHeadToHead: boolean;
}

export interface UncertaintyOption {
  name: string;
  active?: ActiveUncertaintyProperties;
  eliminated?: boolean;
}

export interface ActiveUncertaintyProperties {
  results: {
    heads: FlipResult[];
    tails: FlipResult[];
  };
  coinStyle: string;
  roundComplete: boolean;
}

export interface FlipResult {
  coinStyle: string;
  flippedBy: string;
  waitTime: number;
  flipTime: number;
}

