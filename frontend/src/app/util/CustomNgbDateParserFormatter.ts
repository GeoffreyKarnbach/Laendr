import { Injectable } from '@angular/core';
import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { NgbDateUtil } from './NgbDateUtil';

/**
 * This Service handles how the date is rendered and parsed from keyboard i.e. in the bound input field.
 */
@Injectable()
export class CustomNgbDateParserFormatter extends NgbDateParserFormatter {
  readonly DELIMITER = '.';

  parse(value: string): NgbDateStruct | null {
    if (value) {
      const date = value.split(this.DELIMITER);
      return {
        day: parseInt(date[0], 10),
        month: parseInt(date[1], 10),
        year: parseInt(date[2], 10),
      };
    }
    return null;
  }

  format(date: NgbDateStruct | null): string {
    if (date === null) {
      return null;
    }
    return NgbDateUtil.ngbDateToATString(date);
  }
}
