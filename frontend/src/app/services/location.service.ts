import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from 'src/app/global';
import { LocationDto, Pageable, LocationTagCollectionDto, LocationFilterDto } from 'src/app/dtos';
import { LocationSortingCriterion } from 'src/app/enum';

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  private locationBaseUri: string = this.globals.backendUri + '/locations';
  private locationSearchUri: string = this.locationBaseUri + '/search';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Searches for locations based on if the given query string is contained within
   * a location's or it's lender's name.
   *
   * The result is given as page of all results from the query, sorted by the given
   * or the default criterion.
   *
   * @param q The query string
   * @param page The page to request (starts at 0)
   * @param pageSize The size of the requested page
   * @param sort Criterion to sort locations by (default: recommended)
   * @returns A wrapper for the found location with page information
   */
  search(
    q: string,
    page: number,
    pageSize: number,
    sort: LocationSortingCriterion = LocationSortingCriterion.RECOMMENDED_DESC
  ): Observable<Pageable<LocationDto>> {
    console.log(`Search for locations with q=${q}, page=${page}, pageSize=${pageSize}`);
    return this.httpClient.get<Pageable<LocationDto>>(`${this.locationBaseUri}/search`, {
      params: { q, page, pageSize, sort },
    });
  }

  /**
   * Filters for locations based on the given Filter Information.
   *
   * The result is given as page of all results from the query, sorted by the given
   * or the default criterion.
   *
   * @param filter The query string
   * @param page The page to request (starts at 0)
   * @param pageSize The size of the requested page
   * @param sort Criterion to sort locations by (default: recommended)
   * @returns A wrapper for the found location with page information
   */
  filter(
    filterDto: LocationFilterDto,
    page: number,
    pageSize: number,
    sort: LocationSortingCriterion = LocationSortingCriterion.RECOMMENDED_DESC
  ): Observable<Pageable<LocationDto>> {
    console.log(filterDto);
    console.log(`Filter locations with filter(
      searchString=${filterDto.searchString},
      plz=${filterDto.plz},
      state=${filterDto.state},
      address=${filterDto.address},
      timeFrom=${filterDto.timeFrom},
      timeTo=${filterDto.timeTo},
      priceFrom=${filterDto.priceFrom},
      priceTo=${filterDto.priceTo}),
      page=${page}, pageSize=${pageSize}`);
    return this.httpClient.post<Pageable<LocationDto>>(this.locationSearchUri, filterDto, {
      params: { page, pageSize, sort },
    });
  }

  /**
   * Searches for locations based on the id of the lender of the location, orders
   * results by the given or default criterion.
   *
   * @param id The id of the lender of the location
   * @param includeRemovedLocations Flag whether to include removed locations in the result
   * @param page The page to request (first is 0)
   * @param pageSize The size of the requested page
   * @param sort Criterion to sort locations by (default: recommended)
   * @returns A wrapper for the found location with page information
   */
  searchByLender(
    id: number,
    includeRemovedLocations: boolean,
    page: number,
    pageSize: number,
    sort: LocationSortingCriterion = LocationSortingCriterion.RECOMMENDED_DESC
  ): Observable<Pageable<LocationDto>> {
    return this.httpClient.get<Pageable<LocationDto>>(`${this.locationBaseUri}/search/lender`, {
      params: { id, page, includeRemovedLocations, pageSize, sort },
    });
  }

  /**
   * Creates a new location with the given data.
   *
   * @param location The location to create
   * @returns The created location (with id)
   */
  create(location: LocationDto): Observable<LocationDto> {
    return this.httpClient.post<LocationDto>(`${this.locationBaseUri}`, location);
  }

  /**
   * Updates the location with the given id with the given data.
   *
   * @param id The id of the location to update
   * @param location The location data to update
   * @returns The updated location
   */
  update(id: number, location: LocationDto): Observable<LocationDto> {
    return this.httpClient.put<LocationDto>(`${this.locationBaseUri}/${id}`, location);
  }

  /**
   * Returns the location with the given id.
   *
   * @param id The id of the location
   * @returns The location with the given id
   */
  getById(id: number): Observable<LocationDto> {
    console.log(`Get location with id=${id}`);
    return this.httpClient.get<LocationDto>(`${this.locationBaseUri}/${id}`);
  }

  /**
   * Removes the location with the given id.
   *
   * @param id The id of the location
   * @returns The removed location
   */
  remove(id: number): Observable<LocationDto> {
    console.log(`Remove location with id=${id}`);
    return this.httpClient.delete<LocationDto>(`${this.locationBaseUri}/${id}`);
  }

  /**
   * Returns all tags that currently exist.
   *
   * @returns All tags that currently exist
   */
  getAllTags(): Observable<LocationTagCollectionDto> {
    return this.httpClient.get<LocationTagCollectionDto>(`${this.locationBaseUri}/tags`);
  }
}
