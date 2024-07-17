import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from 'src/app/global';
import { Plz } from 'src/app/dtos';

@Injectable({
  providedIn: 'root',
})
export class PlzService {
  private plzBaseUri: string = this.globals.backendUri + '/plz';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Finds the top 10 plzs that start with the given string.
   *
   * @param plz plz string to search for
   * @returns found plzs
   */
  findSuggestions(plz: string): Observable<Plz[]> {
    console.log(`Search for plz suggestions with plz=${plz}`);
    return this.httpClient.get<Plz[]>(`${this.plzBaseUri}`, {
      params: { plz },
    });
  }
}
