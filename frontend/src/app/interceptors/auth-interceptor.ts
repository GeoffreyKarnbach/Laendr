import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { AuthService } from 'src/app/services';
import { Observable } from 'rxjs';
import { Globals } from 'src/app/global';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private globals: Globals) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + '/authentication';

    // Do not intercept authentication requests
    if (req.url === authUri) {
      return next.handle(req);
    }

    // dont send empty Bearer token, if no user is logged in
    if (!this.authService.isLoggedIn()) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', 'Bearer ' + this.authService.getToken()),
    });

    return next.handle(authReq);
  }
}
