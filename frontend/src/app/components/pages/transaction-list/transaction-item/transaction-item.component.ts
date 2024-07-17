import { Component, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Timeslot, Transaction } from 'src/app/dtos';
import { Role } from 'src/app/enum';

@Component({
  selector: 'app-transaction-item',
  templateUrl: './transaction-item.component.html',
  styleUrls: ['./transaction-item.component.scss'],
})
export class TransactionItemComponent {
  @Input() transaction: Transaction;
  @Input() compact = false;

  constructor(private router: Router, private route: ActivatedRoute) {}

  onClickCard() {
    this.router.navigate(['/transaction', this.transaction.id]);
  }

  isCancelDisabled(): boolean {
    return (
      this.transaction.cancelled &&
      (this.transaction.cancelByRole === this.transaction.ownRoleInTransaction || this.transaction.cancelNotified)
    );
  }

  getPaidAmount(): number {
    if (!this.transaction) return 0;
    if (this.transaction.totalPaid == null) return this.transaction.totalConcerned;
    return this.transaction.totalPaid;
  }

  roleString(): string {
    return this.transaction.ownRoleInTransaction === Role.ROLE_LENDER ? 'Vermieter' : 'Mieter';
  }

  isEndDayEqualToStartDay(timeslot: Timeslot): boolean {
    return (timeslot.start as Date).getDate() === (timeslot.end as Date).getDate();
  }
}
