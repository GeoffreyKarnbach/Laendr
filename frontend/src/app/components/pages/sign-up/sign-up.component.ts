import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, ToastService } from 'src/app/services';
import { SignUpRequest } from 'src/app/dtos';

@Component({
  selector: 'app-sing-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss'],
})
export class SignUpComponent implements OnInit {
  signUpForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.signUpForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required]],
      originalPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
      repeatedPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    });
  }

  /**
   * Form validation will start after the method is called, additionally an SignUpRequest will be sent
   */
  signUpUser() {
    this.submitted = true;
    if (this.signUpForm.valid) {
      const signUpRequest: SignUpRequest = {
        username: this.signUpForm.controls.username.value,
        email: this.signUpForm.controls.email.value,
        originalPassword: this.signUpForm.controls.originalPassword.value,
        repeatedPassword: this.signUpForm.controls.repeatedPassword.value,
      };
      this.createAccount(signUpRequest);
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send sign-up data to the authService. If the sign up was successfully,
   * the user will be forwarded to the home page
   *
   * @param signUpRequest sign.up data from the user sign up form
   */
  createAccount(signUpRequest: SignUpRequest) {
    console.log('Try to create Account for user: ' + signUpRequest.username);
    this.authService.signUpUser(signUpRequest).subscribe({
      next: () => {
        console.log('Successfully created Account for user: ' + signUpRequest.username);
        this.router.navigate(['']);
      },
      error: e => {
        console.log('Could not create Account due to:');
        console.log(e);

        this.toastService.showErrorResponse(e);
      },
    });
  }
}
