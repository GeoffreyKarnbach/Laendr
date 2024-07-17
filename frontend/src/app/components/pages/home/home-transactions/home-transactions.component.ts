import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Transaction } from 'src/app/dtos';
import { Role, TransactionStatus } from 'src/app/enum';
import { ToastService, TransactionService } from 'src/app/services';

@Component({
  selector: 'app-home-transactions',
  templateUrl: './home-transactions.component.html',
  styleUrls: ['./home-transactions.component.scss'],
})
export class HomeTransactionsComponent implements OnInit {
  ongoingTransactions: Transaction[] = [];
  acceptedTransactions: Transaction[] = [];

  constructor(
    private router: Router,
    private transactionService: TransactionService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.getOngoingTransactions();
    this.getAcceptedTransactions();
  }

  onClickToTransactions() {
    this.router.navigate(['/transaction'], {
      queryParams: {
        status: TransactionStatus.ACTIVE,
        role: Role.ROLE_RENTER,
      },
    });
  }

  private getOngoingTransactions() {
    const handleResult = {
      next: res => {
        this.ongoingTransactions = [...this.ongoingTransactions, ...res.result];
        this.ongoingTransactions.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
        this.ongoingTransactions = this.ongoingTransactions.slice(0, 3);
      },
      error: err => {
        this.toastService.showErrorResponse(err);
      },
    };
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACTIVE, Role.ROLE_RENTER, 0, 3)
      .subscribe(handleResult);
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACTIVE, Role.ROLE_LENDER, 0, 3)
      .subscribe(handleResult);
  }

  private getAcceptedTransactions() {
    const handleResult = {
      next: res => {
        this.acceptedTransactions = [...this.acceptedTransactions, ...res.result];
        this.acceptedTransactions.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
        this.acceptedTransactions = this.acceptedTransactions.slice(0, 3);
      },
      error: err => {
        this.toastService.showErrorResponse(err);
      },
    };
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACCEPTED, Role.ROLE_RENTER, 0, 3)
      .subscribe(handleResult);
    this.transactionService
      .getByStatusForRole(TransactionStatus.ACCEPTED, Role.ROLE_LENDER, 0, 3)
      .subscribe(handleResult);
  }
}
