import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Role, roleToDisplayText, statusToDisplayText, TransactionStatus } from 'src/app/enum';
import { AuthService, ToastService, TransactionService } from 'src/app/services';

@Component({
  selector: 'app-cancelled-transactions',
  templateUrl: './cancelled-transactions.component.html',
  styleUrls: ['./cancelled-transactions.component.scss'],
})
export class CancelledTransactionsComponent implements OnInit {
  cancelledAndNotNotified: Record<Role.ROLE_LENDER | Role.ROLE_RENTER, number[]> = { ROLE_LENDER: [], ROLE_RENTER: [] };

  readonly enumStatus = TransactionStatus;
  readonly enumToDisplayStatus = statusToDisplayText;

  readonly enumRole = Role;
  readonly enumToDisplayRole = roleToDisplayText;

  constructor(
    private transactionService: TransactionService,
    private router: Router,
    public authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.getCancelledAndNotNotified();
  }

  onClickCancelWarning() {
    this.router.navigate(['/transaction'], {
      queryParams: {
        status: TransactionStatus.CANCELLED,
        role: this.cancelledAndNotNotified[Role.ROLE_RENTER].length > 0 ? Role.ROLE_RENTER : Role.ROLE_LENDER,
      },
    });
  }

  sumSancelledAndNotNotified(): number {
    return (
      this.cancelledAndNotNotified[Role.ROLE_LENDER].length + this.cancelledAndNotNotified[Role.ROLE_RENTER].length
    );
  }

  isLender() {
    return this.authService.getUserRole().includes(Role.ROLE_LENDER);
  }

  private isLoggedIn() {
    return this.authService.isLoggedIn();
  }

  private getCancelledAndNotNotified() {
    if (this.isLoggedIn()) {
      const handleResult = (role: Role) => ({
        next: res => {
          this.cancelledAndNotNotified[role] = res;
        },
        error: err => {
          this.toastService.showErrorResponse(err);
        },
      });
      this.transactionService
        .getIdsForOutstandingCancelNotificationsForRole(Role.ROLE_RENTER)
        .subscribe(handleResult(Role.ROLE_RENTER));
      if (this.isLender()) {
        this.transactionService
          .getIdsForOutstandingCancelNotificationsForRole(Role.ROLE_LENDER)
          .subscribe(handleResult(Role.ROLE_LENDER));
      }
    }
  }
}
