import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Transaction } from 'src/app/dtos';
import { Role, roleToDisplayText, TransactionStatus, statusToDisplayText } from 'src/app/enum';
import { AuthService, ToastService, TransactionService } from 'src/app/services';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss'],
})
export class TransactionListComponent implements OnInit {
  transactions: Transaction[] = [];
  cancelledAndNotNotified: number[] = [];
  totalAmount: Record<Role.ROLE_LENDER | Role.ROLE_RENTER, Record<TransactionStatus, number>> = {
    ROLE_RENTER: { ACTIVE: 0, ACCEPTED: 0, CANCELLED: 0, COMPLETED: 0, REVIEWED: 0 },
    ROLE_LENDER: { ACTIVE: 0, ACCEPTED: 0, CANCELLED: 0, COMPLETED: 0, REVIEWED: 0 },
  };

  status = TransactionStatus.ACTIVE;
  role!: Role;

  page = 0;
  pageSize = 5;

  readonly enumStatus = TransactionStatus;
  readonly enumToDisplayStatus = statusToDisplayText;

  readonly enumRole = Role;
  readonly enumToDisplayRole = roleToDisplayText;

  isLoading = false;

  constructor(
    private transactionService: TransactionService,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.getTotalAmounts();
    this.route.queryParams.subscribe(params => {
      this.role = params.role;

      this.status = params.status;
      this.getTransactions();

      if (this.status === TransactionStatus.CANCELLED) {
        this.getCancelledAndNotNotified();
      }
    });
  }

  isLender(): boolean {
    return this.authService.getUserRole().includes(Role.ROLE_LENDER);
  }

  onPageChange(event: [page: number, pageSize: number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.getTransactions();
  }

  onClickCancelWarning() {
    this.transactions.forEach(t => (t.cancelNotified = true));
    this.cancelledAndNotNotified.forEach(tid => this.transactionService.recordCancelNotified(tid).subscribe());
    this.cancelledAndNotNotified = [];
  }

  private getTransactions() {
    this.isLoading = true;
    this.transactionService.getByStatusForRole(this.status, this.role, this.page, this.pageSize).subscribe({
      next: res => {
        console.log(res);
        this.transactions = res.result;
        this.isLoading = false;
      },
      error: err => {
        console.error(err);
        this.toastService.showErrorResponse(err);
        this.isLoading = false;
      },
    });
  }

  private getCancelledAndNotNotified() {
    this.transactionService.getIdsForOutstandingCancelNotificationsForRole(this.role).subscribe({
      next: res => {
        console.log(res);
        this.cancelledAndNotNotified = res;
      },
      error: err => {
        console.error(err);
        this.toastService.showErrorResponse(err);
      },
    });
  }

  private getTotalAmounts() {
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACTIVE, Role.ROLE_RENTER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_RENTER.ACTIVE = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACCEPTED, Role.ROLE_RENTER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_RENTER.ACCEPTED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.CANCELLED, Role.ROLE_RENTER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_RENTER.CANCELLED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.COMPLETED, Role.ROLE_RENTER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_RENTER.COMPLETED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.REVIEWED, Role.ROLE_RENTER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_RENTER.REVIEWED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACTIVE, Role.ROLE_LENDER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_LENDER.ACTIVE = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACCEPTED, Role.ROLE_LENDER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_LENDER.ACCEPTED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.CANCELLED, Role.ROLE_LENDER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_LENDER.CANCELLED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.COMPLETED, Role.ROLE_LENDER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_LENDER.COMPLETED = res.totalResults));
    this.transactionService
      .getByStatusForRole(TransactionStatus.REVIEWED, Role.ROLE_LENDER, 0, 1)
      .subscribe(res => (this.totalAmount.ROLE_LENDER.REVIEWED = res.totalResults));
  }
}
