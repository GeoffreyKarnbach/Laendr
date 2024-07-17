import { Injectable } from '@angular/core';
import { AuthRequest, UserInfo } from 'src/app/dtos';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { switchMap, tap } from 'rxjs/operators';
// @ts-ignore
import jwt_decode from 'jwt-decode';
import { Globals } from 'src/app/global';
import { Role } from 'src/app/enum';
import { SignUpRequest } from '../dtos/sign-up-request';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private authBaseUri: string = this.globals.backendUri + '/authentication';
  private SignUpBaseUri: string = this.globals.backendUri + '/signup';

  constructor(private httpClient: HttpClient, private globals: Globals, private userService: UserService) {}

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<UserInfo> {
    return this.httpClient.post(this.authBaseUri, authRequest, { responseType: 'text' }).pipe(
      tap((authResponse: string) => this.setToken(authResponse)),
      switchMap(authResponse => this.storeUserInfo())
    );
  }

  /**
   * Sign Up the new user. If it was successful, a valid JWT token will be stored
   *
   * @param signUpRequest New User data
   */
  signUpUser(signUpRequest: SignUpRequest): Observable<UserInfo> {
    return this.httpClient.post(this.SignUpBaseUri, signUpRequest, { responseType: 'text' }).pipe(
      tap((signUpResponse: string) => this.setToken(signUpResponse)),
      switchMap(signUpResponse => this.storeUserInfo())
    );
  }

  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn(): boolean {
    return !!this.getToken() && this.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf();
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
  }

  getToken(): string {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole(): Role[] {
    if (this.getToken() != null) {
      const decoded: any = jwt_decode(this.getToken());
      const roles: string[] = decoded.rol;
      return roles as any;
    }
    return [];
  }

  /**
   * Returns the email based on the current token
   */
  getUserEmail(): string {
    if (this.getToken() != null) {
      const decoded: any = jwt_decode(this.getToken());
      return decoded.sub as string;
    }
    return null;
  }

  /**
   * Returns the User ID based on the current token
   */
  getUserId(): Observable<number> {
    const email: string = this.getUserEmail();
    if (email != null) {
      return this.userService.getOneByEmail(email);
    } else {
      return of(null);
    }
  }

  /**
   * Saves the JWT token in localStorage
   *
   * @param authResponse the JWT Token
   */
  setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  /**
   * Save short info of user to localStorage
   *
   * @param info short info of user
   */
  saveUserInfo(info: UserInfo) {
    localStorage.setItem('userInfo', JSON.stringify(info));
  }

  /**
   * Get short info of user from localStorage
   *
   * @returns short info of user
   */
  getUserInfo(): UserInfo {
    const stored = localStorage.getItem('userInfo');
    if (stored != null) {
      return JSON.parse(stored);
    }
    return null;
  }

  private getTokenExpirationDate(token: string): Date {
    const decoded: any = jwt_decode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  private storeUserInfo(): Observable<UserInfo> {
    return this.userService.getInfoOfLoggedInUser().pipe(tap(this.saveUserInfo));
  }
}
