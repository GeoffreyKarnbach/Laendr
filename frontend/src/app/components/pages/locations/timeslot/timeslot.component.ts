import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NgbDate, NgbDateParserFormatter, NgbDateStruct, NgbPopover } from '@ng-bootstrap/ng-bootstrap';
import { Timeslot, Transaction, TransactionCreate } from 'src/app/dtos';
import { CancelReason } from 'src/app/enum';
import { AuthService, TimeslotService, ToastService, TransactionService } from 'src/app/services';
import { CustomNgbDateParserFormatter } from 'src/app/util';
import { NgbDateUtil } from 'src/app/util/NgbDateUtil';

@Component({
  selector: 'app-timeslot',
  templateUrl: './timeslot.component.html',
  styleUrls: ['./timeslot.component.scss'],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomNgbDateParserFormatter }],
})
export class TimeslotComponent implements OnChanges {
  @Input() locationId = -1;
  @Input() callerIsLocationOwner = false;

  cache = new Map<string, Timeslot[]>();
  selectedDay: NgbDateStruct;
  selectedDate = new Date();
  selectedTimeslotId: number = null;

  currentTimeslots: Timeslot[];

  initialMessage = ''; //for transaction
  transactionCreate: TransactionCreate = {
    timeslotId: null,
    initialMessage: '',
  };

  isPopoverInCreateMode = true;
  timeslotForUpdateAndCreate?: Timeslot;
  curPopoverTemplateRef?: NgbPopover;

  selectedRowTimeslot?: Timeslot;
  transactionsPage = 0;
  transactionsPageSize = 5;
  transactions: Transaction[] = [];
  transactionsTotalAmount = 0;

  constructor(
    private timeslotService: TimeslotService,
    public authService: AuthService,
    private transactionService: TransactionService,
    private toastService: ToastService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.locationId.currentValue !== -1) {
      this.setTodayAsSelected();
      this.loadTimeslots(NgbDateUtil.ngbDateToISOString(NgbDateUtil.todayAsNgbDateStruct()));
    }
  }

  markDisabledDays: (date: NgbDate, current?: { year: number; month: number }) => boolean = (
    date: NgbDate,
    current?: { year: number; month: number }
  ) => {
    return date.before(NgbDateUtil.todayAsNgbDateStruct());
  };

  daySelectionChanged(date: NgbDateStruct) {
    this.selectedDate = new Date(NgbDateUtil.ngbDateToISOString(date));
    const day = NgbDateUtil.ngbDateToISOString(date);

    if (this.cache.has(day)) {
      this.currentTimeslots = this.cache.get(day);

      if (this.callerIsLocationOwner) {
        this.selectedRowTimeslot = this.currentTimeslots[0];
        if (this.selectedRowTimeslot) {
          this.loadTransactionsForTimeslot();
        }
      }
    } else {
      this.loadTimeslots(day);
    }
  }

  loadTimeslots(day: string) {
    this.timeslotService
      .loadTimeslots({
        locationId: this.locationId,
        day,
        callerIsLocationOwner: this.callerIsLocationOwner,
      })
      .subscribe({
        next: data => {
          this.currentTimeslots = data;
          this.cache.set(day, data);

          if (this.callerIsLocationOwner) {
            this.selectedRowTimeslot = data[0];
            if (data[0]) {
              this.loadTransactionsForTimeslot();
            }
          }
        },
        error: e => {
          console.log(e);
          this.toastService.showErrorResponse(e);
        },
      });
  }

  requestTimeslot(timeslotId: number) {
    this.transactionCreate.initialMessage = this.initialMessage || '';
    this.transactionCreate.timeslotId = timeslotId;
    this.transactionService.startTransaction(this.transactionCreate).subscribe({
      next: data => {
        this.toastService.showSuccess('Anfrage wurde gesendet');
        this.selectedTimeslotId = null;
        this.initialMessage = '';
        this.reloadCurrentlySelectedDay();
      },
      error: error => {
        console.error('Error starting transaction', error);
        this.toastService.showErrorResponse(error);
      },
    });
  }

  isEndDayEqualToStartDay(timeslot: Timeslot): boolean {
    return (timeslot.start as Date).getDate() === (timeslot.end as Date).getDate();
  }

  deleteTimeslot(timeslotId: number) {
    this.timeslotService.deleteTimeslot(timeslotId).subscribe({
      next: noData => {
        this.toastService.showSuccess('Zeitfenster gelöscht');
        this.reloadCurrentlySelectedDay();
      },
      error: e => this.toastService.showErrorResponse(e),
    });
  }

  onOpenEditPopover(timeslot: Timeslot, popover: NgbPopover) {
    this.isPopoverInCreateMode = false;
    this.timeslotForUpdateAndCreate = { ...timeslot };
    this.curPopoverTemplateRef = popover;
  }

  onOpenCreatePopover(popover: NgbPopover) {
    this.isPopoverInCreateMode = true;
    this.curPopoverTemplateRef = popover;
  }

  onCreateOrEditFinisihed() {
    this.reloadCurrentlySelectedDay();
    this.curPopoverTemplateRef?.close();
  }

  onSelectRow(timeslot: Timeslot) {
    if (this.selectedRowTimeslot !== timeslot && this.callerIsLocationOwner) {
      this.selectedRowTimeslot = timeslot;
      if (this.callerIsLocationOwner) {
        this.loadTransactionsForTimeslot();
      }
    }
  }

  onPageChange(event: [page: number, pageSize: number]) {
    this.transactionsPageSize = event[1];
    this.transactionsPage = event[0] - 1;
    this.loadTransactionsForTimeslot();
  }

  loadTransactionsForTimeslot() {
    this.transactionService
      .findOngoingTransactionForTimeslot(this.selectedRowTimeslot.id, this.transactionsPage, this.transactionsPageSize)
      .subscribe({
        next: transactionsPageResult => {
          this.transactions = transactionsPageResult.result;
        },
        error: e => {
          this.toastService.showErrorResponse(e);
        },
      });
  }

  cancelTransaction(transaction: Transaction) {
    this.transactionService
      .recordCancelTransaction({ transactionId: transaction.id, cancelReason: CancelReason.NO_INTEREST })
      .subscribe({
        next: noData => {
          this.toastService.showSuccess('Anfrage erfolgreich abgelehnt');
          this.reloadCurrentlySelectedDay();
        },
        error: e => {
          this.toastService.showErrorResponse(e);
        },
      });
  }

  acceptTransaction(transaction: Transaction) {
    this.transactionService.recordAcceptTransaction(transaction.id).subscribe({
      next: noData => {
        this.toastService.showSuccess('Anfrage erfolgreich angenommen');
        this.reloadCurrentlySelectedDay();
      },
      error: e => {
        this.toastService.showErrorResponse(e);
      },
    });
  }

  returnRequestedTimeslot(timeslot: Timeslot) {
    this.transactionService.cancelTransactionsForTimeslotAsRenter(timeslot.id).subscribe({
      next: noData => {
        this.toastService.showSuccess('Anfrage erfolgreich abgebrochen');
        this.reloadCurrentlySelectedDay();
      },
      error: e => {
        console.log(e);
        this.toastService.showErrorResponse(e);
      },
    });
  }

  returnTimeslotUsedStyleClass() {
    if (this.selectedRowTimeslot.isUsed) {
      return 'border border-success border-3';
    }
    return '';
  }

  private reloadCurrentlySelectedDay() {
    this.loadTimeslots(NgbDateUtil.ngbDateToISOString(this.selectedDay));
  }

  private setTodayAsSelected() {
    this.selectedDay = NgbDateUtil.todayAsNgbDateStruct();
  }
}
