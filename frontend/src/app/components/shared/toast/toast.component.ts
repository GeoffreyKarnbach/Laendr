import { Component, OnDestroy } from '@angular/core';
import { ToastService } from 'src/app/services';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss'],
})
export class ToastComponent implements OnDestroy {
  constructor(public toastService: ToastService) {}

  ngOnDestroy(): void {
    this.toastService.clear();
  }
}
