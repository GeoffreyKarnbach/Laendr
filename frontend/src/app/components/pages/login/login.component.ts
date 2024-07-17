import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, ToastService, UserService } from 'src/app/services';
import { AuthRequest } from 'src/app/dtos';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;

  adminEmail = '';

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(4)]],
    });
    this.userService.getAdminEmail().subscribe(email => {
      console.log('Admin email: ' + email);
      this.adminEmail = email;
    });
  }

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  loginUser() {
    this.submitted = true;
    if (this.loginForm.valid) {
      const authRequest: AuthRequest = {
        email: this.loginForm.controls.username.value,
        password: this.loginForm.controls.password.value,
      };
      this.authenticateUser(authRequest);
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully,
   * the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: AuthRequest) {
    console.log('Try to authenticate user: ' + authRequest.email);
    this.authService.loginUser(authRequest).subscribe({
      next: () => {
        console.log('Successfully logged in user: ' + authRequest.email);
        this.router.navigate(['']);
      },
      error: error => {
        this.toastService.showErrorResponse(error);
        console.log('Could not log in due to:');
        console.log(error);
      },
    });
  }
}
