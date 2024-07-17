import { Component, OnInit } from '@angular/core';
import { NgModel, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserPasswordChangeDto } from 'src/app/dtos';
import { ToastService, UserService } from 'src/app/services';

@Component({
  selector: 'app-passwords',
  templateUrl: './passwords.component.html',
  styleUrls: ['./passwords.component.scss'],
})
export class PasswordsComponent implements OnInit {
  passwordForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  userId: number;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private route: ActivatedRoute,
    private userService: UserService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.passwordForm = this.formBuilder.group({
      currentUserPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
      newUserPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
      repeatedUserPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    });
    this.route.params.subscribe(params => {
      this.userId = params.id;
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public changePassword(): void {
    this.submitted = true;
    if (this.passwordForm.valid) {
      const dto: UserPasswordChangeDto = {
        currentPassword: this.passwordForm.controls.currentUserPassword.value,
        newPassword: this.passwordForm.controls.newUserPassword.value,
        repeatedPassword: this.passwordForm.controls.repeatedUserPassword.value,
      };
      this.userService.changeUserPassword(this.userId, dto).subscribe({
        next: () => {
          this.toastService.showSuccess('Passwort wurde geändert');
          this.resetPasswordDto();
        },
        error: e => this.toastService.showErrorResponse(e),
      });
    }
  }

  private resetPasswordDto() {
    this.passwordForm.controls.currentUserPassword.setValue('');
    this.passwordForm.controls.newUserPassword.setValue('');
    this.passwordForm.controls.repeatedUserPassword.setValue('');
    this.submitted = false;
  }
}
