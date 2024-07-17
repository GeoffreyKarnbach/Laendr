import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Review, Transaction, TransactionCancel } from 'src/app/dtos';
import { CancelReason, reasonToDisplayText, Role, roleToDisplayText } from 'src/app/enum';
import { statusToDisplayText, TransactionStatus } from 'src/app/enum/transaction-status';
import { AuthService, ToastService, TransactionService, ReviewService } from 'src/app/services';

@Component({
  selector: 'app-transaction',
  templateUrl: './transaction.component.html',
  styleUrls: ['./transaction.component.scss'],
})
export class TransactionComponent implements OnInit {
  transaction?: Transaction;
  shouldShowCancelWarning = false;
  error?: boolean;
  showPricePrompt = false;
  pricePromptValue = 0;

  showCancelReason = false;
  cancelReason?: string;
  reasonToDisplayText = reasonToDisplayText;
  selectedCancelReason?: CancelReason;

  constructor(
    public authService: AuthService,
    public transactionService: TransactionService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
    }
    this.fetchTransaction();
  }

  fetchTransaction(): void {
    this.route.params.subscribe(({ id }) => {
      this.transactionService.find(id).subscribe({
        next: res => {
          this.transaction = res;
          this.shouldShowCancelWarning =
            res.cancelled && !res.cancelNotified && res.cancelByRole !== res.ownRoleInTransaction;
        },
        error: err => {
          this.toastService.showErrorResponse(err);
          if (err.status === 403) {
            this.router.navigate(['/']);
          } else {
            this.toastService.showErrorResponse(err);
          }
        },
      });
    });
  }

  onClickCancelWarning() {
    this.shouldShowCancelWarning = false;
    this.transaction.cancelNotified = true;
    this.transactionService.recordCancelNotified(this.transaction.id).subscribe();
  }

  getPaidAmount(): number {
    if (!this.transaction) return 0;
    if (this.transaction.totalPaid == null) return this.transaction.totalConcerned;
    return this.transaction.totalPaid;
  }

  getTransactionStatus(): string {
    if (!this.transaction) return '';
    return statusToDisplayText[this.transactionService.getStatus(this.transaction)];
  }

  getRoleText(): string {
    return roleToDisplayText[this.transaction?.ownRoleInTransaction];
  }

  isNotLender(): boolean {
    return this.transaction?.ownRoleInTransaction !== Role.ROLE_LENDER;
  }

  getPartnerRoleText(): string {
    if (this.isNotLender()) {
      return roleToDisplayText[Role.ROLE_LENDER];
    } else {
      return roleToDisplayText[Role.ROLE_RENTER];
    }
  }

  getCancelByRoleText(): string {
    return roleToDisplayText[this.transaction?.cancelByRole];
  }

  getCancelReason(): string {
    return reasonToDisplayText[this.transaction?.cancelReason];
  }

  displayCompletitionDiv(): boolean {
    return (
      !this.isNotLender() &&
      new Date().getTime() > new Date(this.transaction?.timeslot.end).getTime() &&
      !this.transaction?.completedAt &&
      !this.transaction?.cancelled &&
      this.transaction.timeslot.isUsed
    );
  }

  onClickCompletition() {
    if (!this.showPricePrompt) {
      this.showPricePrompt = true;
      this.pricePromptValue = this.transaction.totalConcerned;
    } else {
      this.transactionService.recordTransactionComplete(this.transaction.id, this.pricePromptValue).subscribe({
        next: () => {
          console.log('Transaction completed');
          this.fetchTransaction();
        },
        error: err => {
          this.toastService.showErrorResponse(err);
        },
      });
    }
  }

  getCompletionButtonText() {
    return !this.showPricePrompt ? 'Abschließen' : 'Preis bestätigen';
  }

  displayCancelationDiv(): boolean {
    console.log(this.transaction);
    return !this.transaction?.cancelled && !this.transaction?.completedAt;
  }

  onClickCancel() {
    if (!this.showCancelReason) {
      this.showCancelReason = true;
    } else {
      const cancelDto: TransactionCancel = {
        transactionId: this.transaction?.id,
        cancelMessage: this.cancelReason,
        cancelReason: this.selectedCancelReason,
      };

      console.log(cancelDto);
      this.transactionService.recordCancelTransaction(cancelDto).subscribe({
        next: () => {
          console.log('Cancelation completed');
          this.fetchTransaction();
        },
        error: err => {
          this.toastService.showErrorResponse(err);
        },
      });
    }
  }

  getCancelationButtonText() {
    return !this.showCancelReason ? 'Reservierung abbrechen' : 'Grund bestätigen';
  }

  sendLocationReview(review: Review) {
    this.reviewService
      .createLocationReview({ transactionId: this.transaction.id, rating: review.rating, comment: review.comment })
      .subscribe({
        next: () => this.fetchTransaction(),
        error: e => this.toastService.showErrorResponse(e),
      });
  }

  sendRenterReview(review: Review) {
    this.reviewService
      .createRenterReview({ transactionId: this.transaction.id, rating: review.rating, comment: review.comment })
      .subscribe({
        next: () => this.fetchTransaction(),
        error: e => this.toastService.showErrorResponse(e),
      });
  }

  showAcceptButton(): boolean {
    return (
      this.transactionService.getStatus(this.transaction) === TransactionStatus.ACTIVE &&
      !this.isNotLender() &&
      new Date().getTime() <= new Date(this.transaction?.timeslot.start).getTime()
    );
  }

  acceptTransaction() {
    this.transactionService.recordAcceptTransaction(this.transaction.id).subscribe({
      next: noData => {
        this.toastService.showSuccess('Anfrage erfolgreich angenommen');
        this.transaction = { ...this.transaction, timeslot: { ...this.transaction.timeslot, isUsed: true } };
      },
      error: e => {
        this.toastService.showErrorResponse(e);
      },
    });
  }
}
