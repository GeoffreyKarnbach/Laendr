import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Globals } from 'src/app/global';
import { Role, TransactionStatus } from 'src/app/enum';
import { ReviewService, ToastService, TransactionService } from 'src/app/services';
import { Transaction } from 'src/app/dtos';
import { ReviewCount } from 'src/app/dtos/review-count';

@Component({
  selector: 'app-review-transactions',
  templateUrl: './review-transactions.component.html',
  styleUrls: ['./review-transactions.component.scss'],
})
export class ReviewTransactionsComponent implements OnInit {
  notReviewedTransactions: Transaction[] = [];
  reviewCount: ReviewCount = {
    reviews: 0,
    completedTransactions: 0,
  };

  constructor(
    private router: Router,
    private toastService: ToastService,
    private transactionService: TransactionService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.getNotReviewedTransactions();
    this.loadReviewInfo();
  }

  onClickToTransactions() {
    this.router.navigate(['/transaction'], {
      queryParams: {
        status: TransactionStatus.ACTIVE,
        role: Role.ROLE_RENTER,
      },
    });
  }

  loadReviewInfo() {
    this.reviewService.getReviewCount().subscribe({
      next: data => {
        this.reviewCount = data;
      },
      error: e => {
        this.toastService.showErrorResponse(e);
      },
    });
  }

  getPercentage(): number {
    //Percentage is rounded to 1 decimal
    return Math.round((this.reviewCount.reviews / this.reviewCount.completedTransactions) * 1000) / 10;
  }

  getPercentageString(): string {
    return this.getPercentage() + '%';
  }

  public getShow(): boolean {
    if (this.reviewCount.completedTransactions === this.reviewCount.reviews) {
      return false;
    } else {
      return true;
    }
  }

  getColor(): string {
    const x = this.getPercentage();
    if (x === 100) {
      return 'gold';
    } else if (x < 100 && x >= 75) {
      return 'yellow';
    } else if (x < 75 && x >= 50) {
      return 'green';
    } else if (x < 50 && x >= 25) {
      return 'blue';
    } else if (x < 25 && x > 0) {
      return 'purple';
    } else {
      return 'grey';
    }
  }

  getShortString(): string {
    const x = this.getPercentage();
    if (x === 100) {
      return 'Exzellent';
    } else if (x < 100 && x >= 75) {
      return 'Überflieger';
    } else if (x < 75 && x >= 50) {
      return 'Durchschnitt';
    } else if (x < 50 && x >= 25) {
      return 'Nachhut';
    } else if (x < 25 && x > 0) {
      return 'Nachzügler';
    } else {
      return 'Anfänger';
    }
  }

  //returns a string depending on percentage of reviews
  getBarString(): string {
    const x = this.getPercentage();
    if (x === 100) {
      return (
        'Sehr gut! Danke für deine Unterstützung andere Nutzer zu bewerten ' +
        'und uns eine gute Qualität auf Ländr zu ermöglichen.'
      );
    } else if (x < 100 && x >= 75) {
      return (
        'Danke, dass du dabei hilfst, für eine gute Qualität auf Ländr zu sorgen. ' +
        'Du hast zwar schon einen Großteil bewertet, aber es gibt noch mehr, das du bewerten könntest.'
      );
    } else if (x < 75 && x >= 50) {
      return (
        'Schön dass du bereit bist, dabei zu helfen gute Nutzer auf Ländr hervorzuheben. ' +
        'Je mehr du bewertest, desto bessere Empfehlungen können wir dir in Zukunft anbieten.'
      );
    } else if (x < 50 && x >= 25) {
      return (
        'Es freut uns, dass du auf Ländr einige Angebote angenommen hast. ' +
        'Um dir weiterhin gute Angebote zu präsentieren, brauchen wir deine Unterstützung. ' +
        'Bitte bewerte deine bisherigen Transaktionen.'
      );
    } else if (x < 25 && x > 0) {
      return (
        'Wir hoffen, du hast gute Erfahrungen auf Ländr gemacht. ' +
        'Du könnstest uns dabei helfen dir weiterhin gute Angebote ' +
        'zu machen, indem du einige deiner bisherigen Transaktionen bewerten würdest.'
      );
    } else {
      return (
        'Wir sehen du hast bereits etwas auf Ländr gefunden, was dir zugesagt hat. ' +
        'Es wäre sehr hilfreich, wenn du deine Erfahrungen als Review teilst. ' +
        'Wir werden dein Feedback nutzen, um unser Angebot für dich und andere anzupassen.'
      );
    }
  }

  private getNotReviewedTransactions() {
    const handleResult = {
      next: res => {
        this.notReviewedTransactions = [...this.notReviewedTransactions, ...res.result];
        this.notReviewedTransactions.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
        this.notReviewedTransactions = this.notReviewedTransactions.slice(0, 3);
      },
      error: err => {
        this.toastService.showErrorResponse(err);
      },
    };
    this.transactionService
      .getByStatusForRole(TransactionStatus.COMPLETED, Role.ROLE_RENTER, 0, 3)
      .subscribe(handleResult);
    this.transactionService
      .getByStatusForRole(TransactionStatus.COMPLETED, Role.ROLE_LENDER, 0, 3)
      .subscribe(handleResult);
  }
}
