import { Pipe, PipeTransform } from '@angular/core';
import { HumanizeDurationLanguage, HumanizeDuration } from 'humanize-duration-ts';

/**
 * Wrapper around humanize-duration to implement it as a pipe with short English suffixes.
 */
@Pipe({
  name: 'humanizeDuration'
})
export class HumanizeDurationPipe implements PipeTransform {

  langService: HumanizeDurationLanguage = new HumanizeDurationLanguage();
  humanizer: HumanizeDuration = new HumanizeDuration(this.langService);

  constructor() {
    this.humanizer.addLanguage('shortEn', {
      y: () => 'y',
      mo: () => 'mo',
      w: () => 'w',
      d: () => 'd',
      h: () => 'h',
      m: () => 'm',
      s: () => 's',
      ms: () => 'ms',
      decimal: '.',
    });
  }

  transform(value: number): string {
    value = +(value).toFixed(2) * 1000;
    return this.humanizer.humanize(+value, {
      language: 'shortEn'
    });
  }

}
