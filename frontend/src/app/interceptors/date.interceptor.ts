import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpEventType } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { LocationDto } from 'src/app/dtos';

@Injectable()
export class DateInterceptor implements HttpInterceptor {
  constructor() {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      map(event => {
        if (event.type === HttpEventType.Response) {
          this.mapAllDateStrings(event.body);
        }
        return event;
      })
    );
  }

  private mapAllDateStrings(input: any): any {
    if (typeof input === 'object' && input != null) {
      Object.keys(input).forEach(key => {
        const value = input[key];
        if (typeof value === 'string') {
          input[key] = this.mapStringToDate(value);
        } else if (value && typeof value === 'object') {
          input[key] = this.mapAllDateStrings(value);
        }
      });
    }
    return input;
  }

  private mapStringToDate(possibleDateStr: string): string | Date {
    if (!/\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}:\d{2}.\d+)?/.test(possibleDateStr)) {
      return possibleDateStr;
    }
    return new Date(possibleDateStr);
  }
}
