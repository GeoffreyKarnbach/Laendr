import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgbDateParserFormatter, NgbDateStruct, NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { Timeslot } from 'src/app/dtos';
import { TimeslotService, ToastService } from 'src/app/services';
import { CustomNgbDateParserFormatter, NgbDateUtil } from 'src/app/util';

@Component({
  selector: 'app-timeslot-create-edit',
  templateUrl: './timeslot-create-edit.component.html',
  styleUrls: ['./timeslot-create-edit.component.scss'],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomNgbDateParserFormatter }],
})
export class TimeslotCreateEditComponent implements OnInit, AfterViewInit {
  @Input() timeslot: Timeslot;
  @Input() isPopoverInCreateMode = false;
  @Input() selectedDay: NgbDateStruct;
  @Input() locationId: number;

  @Output() createOrUpdateFinished = new EventEmitter<void>();

  defaultTimeslot: Timeslot = {
    start: new Date(),
    end: new Date(),
    price: 10,
  };
  defaultTimeStruct: NgbTimeStruct = {
    hour: 0,
    minute: 0,
    second: 0,
  };
  startTime: NgbTimeStruct = { ...this.defaultTimeStruct };
  endTime: NgbTimeStruct = { ...this.defaultTimeStruct };
  endDate: NgbDateStruct;

  inputElements: HTMLCollectionOf<Element>;

  constructor(private timeslotService: TimeslotService, private toastService: ToastService) {}

  ngAfterViewInit(): void {
    this.inputElements = document.getElementsByClassName('ngb-tp-input form-control form-control-sm');

    for (let i = 0; i < this.inputElements.length; i++) {
      this.inputElements.item(i).addEventListener('blur', e => this.updateUIWithCurrentValues());
    }
  }

  ngOnInit() {
    if (this.isPopoverInCreateMode) {
      this.initCreateMode();
    } else {
      this.initEditMode();
    }
  }

  createTimeslot() {
    this.buildCreateDto();

    this.timeslotService.createTimeslot(this.timeslot).subscribe({
      next: createdTimeslot => {
        console.log('created timeslot', createdTimeslot);
        this.toastService.showSuccess('Zeitfenster erstellt');
        this.createOrUpdateFinished.emit();
      },
      error: e => this.toastService.showErrorResponse(e),
    });
  }

  updateTimeslot() {
    this.buildUpdateDto();

    this.timeslotService.updateTimeslot(this.timeslot.id, this.timeslot).subscribe({
      next: updatedTimeslot => {
        console.log('updated timeslot', updatedTimeslot);
        this.toastService.showSuccess('Zeitfenster aktualisiert');
        this.createOrUpdateFinished.emit();
      },
      error: e => this.toastService.showErrorResponse(e),
    });
  }

  inputValid(): boolean {
    const startTwentyFourHour = this.startTime.hour >= 0 && this.startTime.hour < 24;
    const endTwentyFourHour = this.endTime.hour >= 0 && this.endTime.hour < 24;
    const startBeforeEnd = NgbDateUtil.ngbDateTimeFirstIsBeforeSecond(
      this.selectedDay,
      this.startTime,
      this.endDate,
      this.endTime
    );
    const priceValid = this.timeslot.price > 0;
    return startTwentyFourHour && endTwentyFourHour && startBeforeEnd && priceValid;
  }

  updateUIWithCurrentValues() {
    this.inputElements.item(0)['value'] = NgbDateUtil.prefixZeroIfLessThanTen(this.startTime.hour);
    this.inputElements.item(1)['value'] = NgbDateUtil.prefixZeroIfLessThanTen(this.startTime.minute);
    this.inputElements.item(2)['value'] = NgbDateUtil.prefixZeroIfLessThanTen(this.endTime.hour);
    this.inputElements.item(3)['value'] = NgbDateUtil.prefixZeroIfLessThanTen(this.endTime.minute);
  }

  private buildCreateDto() {
    this.timeslot.locationId = this.locationId;
    this.buildUpdateDto();
  }

  private buildUpdateDto() {
    this.timeslot.start = this.dateAndTimetoString(this.selectedDay, this.startTime);
    this.timeslot.end = this.dateAndTimetoString(this.endDate, this.endTime);
  }

  private dateAndTimetoString(date: NgbDateStruct, time: NgbTimeStruct): string {
    const hour = NgbDateUtil.prefixZeroIfLessThanTen(time.hour);
    const minute = NgbDateUtil.prefixZeroIfLessThanTen(time.minute);
    return `${NgbDateUtil.ngbDateToISOString(date)}T${hour}:${minute}`;
  }

  private initCreateMode() {
    const currentHour = new Date().getHours();
    this.startTime = { ...this.defaultTimeStruct, hour: currentHour };
    this.endTime = { ...this.defaultTimeStruct, hour: (currentHour + 1) % 24 };
    this.endDate = { ...this.selectedDay };

    this.timeslot = {
      ...this.defaultTimeslot,
      start: NgbDateUtil.ngbDateStructToDate(this.selectedDay),
    };
  }

  private initEditMode() {
    this.startTime = NgbDateUtil.dateToNgbTimeStruct(this.timeslot.start as Date);
    this.endTime = NgbDateUtil.dateToNgbTimeStruct(this.timeslot.end as Date);
    this.endDate = NgbDateUtil.dateToNgbDateStruct(this.timeslot.end as Date);
  }
}
