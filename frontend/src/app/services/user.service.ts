import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Globals } from '../global';
import { Pageable, UserDto, UserInfo, UserPasswordChangeDto } from '../dtos';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private userBaseUri: string = this.globals.backendUri + '/user';
  private changePasswordUri = 'change-password';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * This searches for a user given his id
   *
   * @param id The id of the user
   * @returns The user if found
   */
  getOneById(id: number): Observable<UserDto> {
    console.log(`Get user with id=${id}`);
    const url = `${this.userBaseUri}/${id}`;
    return this.httpClient.get<UserDto>(url);
  }

  /**
   * This searches for a user given by his email
   *
   * @param id The id of the user
   * @returns The user if found
   */
  getOneByEmail(email: string): Observable<number> {
    console.log(`Get user with email=${email}`);
    return this.httpClient.get<number>(`${this.userBaseUri}`, {
      params: { email },
    });
  }

  /**
   * Get short info of logged-in user
   *
   * @returns short info of the user
   */
  getInfoOfLoggedInUser(): Observable<UserInfo> {
    return this.httpClient.get<UserInfo>(`${this.userBaseUri}/info`);
  }

  /**
   * This updates the password for a user given by valid password and new valid password
   *
   * @param id The id of the user
   * @param dto The passwords
   */
  changeUserPassword(id: number, dto: UserPasswordChangeDto) {
    console.log(`Update password for user with id = ${id}`);
    return this.httpClient.put(`${this.userBaseUri}/${id}/${this.changePasswordUri}`, dto);
  }

  /**
   * Updates the user with the given id with the given data.
   *
   * @param id The id of the user to update
   * @param user The user data to update
   * @returns The updated user
   */
  update(id: number, user: UserDto): Observable<UserDto> {
    return this.httpClient.put<UserDto>(`${this.userBaseUri}/${id}`, user);
  }

  /**
   * Deletes a given user.
   *
   * @param id ID of the given user
   * @returns observable of void, only useful for error handling
   */
  deleteUser(id: number): Observable<void> {
    return this.httpClient.delete<void>(this.userBaseUri + '/' + id);
  }

  /**
   * Returns all users (locked and unlocked)
   *
   * @param locked true if only locked users should be returned
   * @param page the page to return
   * @param pageSize the size of the page
   * @returns all users
   */
  getAllUsers(locked: boolean, page: number, pageSize: number): Observable<Pageable<UserDto>> {
    return this.httpClient.get<Pageable<UserDto>>(this.userBaseUri + '/all', { params: { locked, page, pageSize } });
  }

  /**
   * Locks a user given by his ID (no admin is lockable)
   *
   * @param id The id of the user to lock
   * @returns observable of void, only useful for error handling
   */
  lockUser(id: number): Observable<void> {
    return this.httpClient.put<void>(this.userBaseUri + '/lock/' + id, null);
  }

  /**
   * Unlocks a user given by his ID
   *
   * @param id The id of the user to unlock
   * @returns observable of void, only useful for error handling
   */
  unlockUser(id: number): Observable<void> {
    return this.httpClient.put<void>(this.userBaseUri + '/unlock/' + id, null);
  }

  /**
   * Returns an email address of an admin
   *
   * @returns email address of an admin
   */
  getAdminEmail(): Observable<string> {
    return this.httpClient.get<string>(this.globals.backendUri + '/authentication/admin-email', {
      responseType: 'text' as 'json',
    });
  }
}
