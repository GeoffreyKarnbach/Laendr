import { Pipe, PipeTransform } from '@angular/core';
import { AustriaState, stateToStateText } from 'src/app/enum';

@Pipe({
  name: 'enumIntToDescription',
})
export class EnumBundeslandToDescription implements PipeTransform {
  transform(value: AustriaState): string {
    return stateToStateText[value];
  }
}
