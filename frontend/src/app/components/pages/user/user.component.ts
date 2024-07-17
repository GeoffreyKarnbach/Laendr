import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, debounceTime, switchMap } from 'rxjs';
import { UserDto, Plz } from 'src/app/dtos';
import { Role } from 'src/app/enum';
import { AuthService, PlzService, ToastService, UserService } from 'src/app/services';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { LatLngLiteral } from 'leaflet';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
})
export class UserComponent implements OnInit {
  userId: number;
  user: UserDto = {
    name: '',
    email: '',
    locked: false,
    loginAttempts: 0,
    lenderDescription: '',
    plz: { plz: '', ort: '' },
    state: null,
    lender: false,
    createdAt: null,
    coordinates: null,
  };

  plzInputChange = new Subject<string>();
  plzSuggestions: Plz[] = [];

  userDataHasBeenChanged = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private plzService: PlzService,
    private toastService: ToastService,
    private modalService: NgbModal,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.userId = params.id;
      this.userService.getOneById(this.userId).subscribe({
        next: user => {
          if (user.plz === null) {
            user.plz = { plz: '', ort: '' };
          }
          this.user = user;
          console.log(this.user);
        },
        error: e => {
          this.toastService.showErrorResponse(e);
          this.router.navigate(['']);
        },
      });
    });

    this.plzInputChange
      .pipe(
        debounceTime(300),
        switchMap(val => this.plzService.findSuggestions(val))
      )
      .subscribe({
        next: plzs => {
          this.plzSuggestions = plzs;
          this.userDataHasBeenChanged = true;
        },
        error: e => {
          this.toastService.showErrorResponse(e);
        },
      });
  }

  deleteUser() {
    const modalRef = this.modalService.open(ConfirmationModalComponent);
    modalRef.componentInstance.message = 'Sind Sie sich sicher? Diese Aktion kann nicht rückgängig gemacht werden!';
    modalRef.result.then(result => {
      if (result === 'confirm') {
        this.userService.deleteUser(this.userId).subscribe({
          next: () => {
            this.toastService.showSuccess('User wurde gelöscht');
            if (this.userId === this.authService.getUserInfo()?.id) {
              this.authService.logoutUser();
            }
            this.router.navigate(['']);
          },
          error: e => this.toastService.showErrorResponse(e),
        });
      }
    });
  }

  coordChanged(coords: LatLngLiteral) {
    this.userDataHasBeenChanged = true;
    this.user.coordinates = coords;

    this.changeDetectorRef.detectChanges();
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    if (form.valid) {
      this.userService.update(this.userId, this.user).subscribe({
        next: user => {
          if (user.plz === null) {
            user.plz = { plz: '', ort: '' };
          }
          this.user = user;
          this.toastService.showSuccess('Informationen wurde geändert');

          this.userDataHasBeenChanged = false;

          const userInfo = this.authService.getUserInfo();
          this.authService.saveUserInfo({
            ...userInfo,
            coordinates: user.coordinates,
          });
        },
        error: e => this.toastService.showErrorResponse(e),
      });
    }
  }

  isAdminView(): boolean {
    return this.authService.getUserRole().includes(Role.ROLE_ADMIN);
  }

  unlockUser() {
    this.userService.unlockUser(this.userId).subscribe(() => {
      this.user.locked = false;
      console.log('UNLOCKED');
      console.log(this.user);
    });
  }

  lockUser() {
    this.userService.lockUser(this.userId).subscribe(() => {
      this.user.locked = true;
      console.log('LOCKED');
      console.log(this.user);
    });
  }
}

@Component({
  selector: 'app-confirmation-modal',
  template: `
    <div class="modal-header">
      <h4 class="modal-title">Bestätigung</h4>
      <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss()">
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
    <div class="modal-body">
      {{ message }}
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-secondary" (click)="activeModal.dismiss('cancel')">Nein</button>
      <button type="button" class="btn btn-danger" (click)="activeModal.close('confirm')">Ja</button>
    </div>
  `,
})
export class ConfirmationModalComponent {
  @Input() message: string;

  constructor(public activeModal: NgbActiveModal) {}
}
