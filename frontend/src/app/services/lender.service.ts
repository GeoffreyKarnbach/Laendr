import { Injectable } from '@angular/core';
import { LenderDto } from '../dtos/lender';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Globals } from '../global';

@Injectable({
  providedIn: 'root',
})
export class LenderService {
  private lenderBaseUri: string = this.globals.backendUri + '/lender';

  constructor(private http: HttpClient, private globals: Globals) {}

  /**
   * This searches for a lender given by his id
   *
   * @param id The id of the lender
   * @returns The lender if found
   */
  getOneById(id: number): Observable<LenderDto> {
    const url = `${this.lenderBaseUri}/${id}`;
    return this.http.get<LenderDto>(url);
  }

  /**
   * Attempts to add the lender role to a given user
   *
   * @param id ID of the user to add the lender role to (must be current user)
   * @returns observable for a new JWT if neccessary
   */
  addLenderRole(id: number): Observable<string | null> {
    return this.http.post<string | null>(this.lenderBaseUri + '/role/' + id, {}, { responseType: 'text' as 'json' });
  }
}
