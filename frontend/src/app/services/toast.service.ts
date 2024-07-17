import { Injectable } from '@angular/core';
import { ErrorResponse, ToastInfo } from '../dtos';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  toasts: ToastInfo[] = [];
  defaultDelay = 4000;

  remove(toast: ToastInfo) {
    this.toasts = this.toasts.filter(t => t !== toast);
  }

  clear() {
    this.toasts.splice(0, this.toasts.length);
  }

  showStandard(body: string, header: string = null) {
    this.toasts.push({ header, body, delay: this.defaultDelay });
  }

  showSuccess(body: string, header: string = null) {
    this.toasts.push({ header, body, classname: 'bg-success text-light', delay: this.defaultDelay });
  }

  showError(body: string, header: string = null) {
    this.toasts.push({ header, body, classname: 'bg-danger text-light', delay: this.defaultDelay });
  }

  showErrorResponse(error: HttpErrorResponse) {
    const toastInfo: ToastInfo = {
      body: null,
      classname: 'bg-danger text-light',
      delay: this.defaultDelay,
    };

    switch (error.status) {
      case 0:
        toastInfo.body = 'Server ist nicht erreichbar.';
        break;
      case 400:
        // Spring validation
        const errorObject = JSON.parse(error.error);
        toastInfo.header = errorObject.message;
        toastInfo.body = errorObject.errors[0]?.errors?.map(element => `${element.field} ${element.message} \n`);
        break;
      case 403:
        // Forbidden
        toastInfo.body = 'Zugriff verweigert.';
        break;
      case 404:
        // Not Found
        toastInfo.body = error.error;
        break;
      case 405:
        // Not allowed
        toastInfo.body = 'Ein Fehler ist aufgetreten.';
        break;
      case 500:
        // Internal Server Error
        toastInfo.body = 'Ein Fehler ist aufgetreten.';
        break;
      default:
        // Custom validation
        const errorResponse: ErrorResponse | string = error.error;
        if (typeof errorResponse === 'string') {
          try {
            const parsed = JSON.parse(errorResponse);
            this.toastInfoFromErrorResponse(parsed, toastInfo);
          } catch (err) {
            toastInfo.body = errorResponse;
          }
        } else {
          this.toastInfoFromErrorResponse(errorResponse, toastInfo);
        }
        break;
    }

    this.toasts.push(toastInfo);
  }

  private toastInfoFromErrorResponse(errorResponse: ErrorResponse, toastInfo: ToastInfo) {
    const body = errorResponse?.errors?.map(e => e.message).join('\n');
    toastInfo.header = body ? errorResponse.message : null;
    toastInfo.body = body ? body : errorResponse.message;
  }
}
