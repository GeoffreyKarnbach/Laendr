import { NgbDate, NgbDateStruct, NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';

export class NgbDateUtil {
  static todayAsNgbDateStruct(): NgbDateStruct {
    const today = new Date();
    return NgbDateUtil.dateToNgbDateStruct(today);
  }

  static ngbDateToISOString(date: NgbDateStruct): string {
    return `${date.year}-${this.prefixZeroIfLessThanTen(date.month)}-${this.prefixZeroIfLessThanTen(date.day)}`;
  }

  static ngbDateToATString(date: NgbDateStruct): string {
    return `${this.prefixZeroIfLessThanTen(date.day)}.${this.prefixZeroIfLessThanTen(date.month)}.${date.year}`;
  }

  static prefixZeroIfLessThanTen(i: number): string {
    return i < 10 ? '0' + i : i.toString();
  }

  static dateToNgbTimeStruct(date: Date): NgbTimeStruct {
    return {
      hour: date.getHours(),
      minute: date.getMinutes(),
      second: date.getSeconds(),
    };
  }

  static dateToNgbDateStruct(date: Date): NgbDateStruct {
    return {
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
    };
  }

  static ngbDateStructToDate(dateStruct: NgbDateStruct): Date {
    return new Date(NgbDateUtil.ngbDateToISOString(dateStruct));
  }

  static ngbTimeFirstIsBeforeSecond(first: NgbTimeStruct, second: NgbTimeStruct): boolean {
    return (
      first.hour < second.hour ||
      (first.hour === second.hour && first.minute < second.minute) ||
      (first.hour === second.hour && first.minute === second.minute && first.second < second.second)
    );
  }

  static ngbDateTimeFirstIsBeforeSecond(
    firstDate: NgbDateStruct,
    firstTime: NgbTimeStruct,
    secondDate: NgbDateStruct,
    secondTime: NgbTimeStruct
  ): boolean {
    const fd = NgbDate.from(firstDate);
    const sd = NgbDate.from(secondDate);

    return fd.before(sd) || (fd.equals(sd) && this.ngbTimeFirstIsBeforeSecond(firstTime, secondTime));
  }
}
