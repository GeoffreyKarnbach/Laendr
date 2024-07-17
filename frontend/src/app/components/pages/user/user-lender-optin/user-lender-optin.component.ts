import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, LenderService, ToastService } from 'src/app/services';

@Component({
  selector: 'app-user-lender-optin',
  templateUrl: './user-lender-optin.component.html',
  styleUrls: ['./user-lender-optin.component.scss'],
})
export class UserLenderOptinComponent implements OnInit {
  userId?: number = null;

  constructor(
    private authService: AuthService,
    private lenderService: LenderService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.userId = params.id;
    });
  }

  becomeLender() {
    if (this.userId) {
      this.lenderService.addLenderRole(this.userId).subscribe({
        next: token => {
          if (token) {
            this.authService.setToken(token);
          }
          this.toastService.showSuccess('User erfolgreich zu Vermieter gemacht');
          this.router.navigate(['']);
        },
        error: e => {
          this.toastService.showErrorResponse(e);
          this.router.navigate(['']);
        },
      });
    }
  }
}
