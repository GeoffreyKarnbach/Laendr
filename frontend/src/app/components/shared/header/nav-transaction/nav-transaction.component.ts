import { Component, OnInit } from '@angular/core';
import { Role, TransactionStatus } from 'src/app/enum';
import { AuthService, ToastService, TransactionService } from 'src/app/services';

@Component({
  selector: 'app-nav-transaction',
  templateUrl: './nav-transaction.component.html',
  styleUrls: ['./nav-transaction.component.scss'],
})
export class NavTransactionComponent implements OnInit {
  cancelledAndNotNotifiedAmountForRenter = 0;
  cancelledAndNotNotifiedAmountForLender = 0;
  amountToReviewForRenter = 0;
  amountToReviewForLender = 0;
  isNotifications = false;
  isRenterNotifications = false;
  isLenderNotifications = false;

  readonly enumStatus = TransactionStatus;
  readonly enumRole = Role;

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.transactionService.getIdsForOutstandingCancelNotificationsForRole(Role.ROLE_RENTER).subscribe({
      next: res => {
        console.log(res);
        this.cancelledAndNotNotifiedAmountForRenter = res.length;
        if (res.length) {
          this.isNotifications = true;
          this.isRenterNotifications = true;
        }
      },
      error: err => {
        console.error(err);
        this.toastService.showErrorResponse(err);
      },
    });

    this.transactionService.countAllNotReviewedForRole(Role.ROLE_RENTER).subscribe({
      next: res => {
        console.log(res);
        this.amountToReviewForRenter = res;
        if (res) {
          this.isNotifications = true;
          this.isRenterNotifications = true;
        }
      },
      error: err => {
        console.error(err);
        this.toastService.showErrorResponse(err);
      },
    });

    if (this.isLender()) {
      this.transactionService.getIdsForOutstandingCancelNotificationsForRole(Role.ROLE_LENDER).subscribe({
        next: res => {
          console.log(res);
          this.cancelledAndNotNotifiedAmountForLender = res.length;
          if (res.length) {
            this.isNotifications = true;
            this.isLenderNotifications = true;
          }
        },
        error: err => {
          console.error(err);
          this.toastService.showErrorResponse(err);
        },
      });

      this.transactionService.countAllNotReviewedForRole(Role.ROLE_LENDER).subscribe({
        next: res => {
          console.log(res);
          this.amountToReviewForLender = res;
          if (res) {
            this.isNotifications = true;
            this.isLenderNotifications = true;
          }
        },
        error: err => {
          console.error(err);
          this.toastService.showErrorResponse(err);
        },
      });
    }
  }

  isLender(): boolean {
    return this.authService.getUserRole().includes(Role.ROLE_LENDER);
  }
}
