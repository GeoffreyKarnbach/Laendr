import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from 'src/app/global';
import { Timeslot, TimeslotSearch } from 'src/app/dtos';

@Injectable({
  providedIn: 'root',
})
export class TimeslotService {
  private timeslotBaseUri = this.globals.backendUri + '/timeslots';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Find all timeslots for a location on a certain day
   *
   * @returns all timeslots for the given location on the given day
   */
  loadTimeslots(search: TimeslotSearch): Observable<Timeslot[]> {
    return this.httpClient.get<Timeslot[]>(this.timeslotBaseUri, { params: { ...search } });
  }

  /**
   * Delete the timeslot with the passed id
   *
   * @param id id of the timeslot to be deleted
   */
  deleteTimeslot(id: number): Observable<void> {
    return this.httpClient.delete<void>(this.timeslotBaseUri + `/${id}`);
  }

  /**
   * Update a timeslot with the passed id
   *
   * @param id id of the timeslot to be updated
   * @param timeslot new values for the timeslot
   * @returns the updated timeslot
   */
  updateTimeslot(id: number, timeslot: Timeslot): Observable<Timeslot> {
    return this.httpClient.put<Timeslot>(this.timeslotBaseUri + `/${id}`, timeslot);
  }

  /**
   * Create a new timeslot for a location
   *
   * @param timeslot timeslot to be created
   * @returns the persisted timeslot
   */
  createTimeslot(timeslot: Timeslot): Observable<Timeslot> {
    return this.httpClient.post<Timeslot>(this.timeslotBaseUri, timeslot);
  }
}
