import { Component, OnInit } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LatLngLiteral } from 'leaflet';
import { debounceTime, Subject, switchMap, tap } from 'rxjs';
import { LocationDto, Plz } from 'src/app/dtos';
import { LocationService, PlzService, ToastService } from 'src/app/services';

export enum LocationCreateEditModes {
  CREATE,
  EDIT,
}

@Component({
  selector: 'app-create-edit-location',
  templateUrl: './create-edit-location.component.html',
  styleUrls: ['./create-edit-location.component.scss'],
})
export class CreateEditLocationComponent implements OnInit {
  isEditMode: LocationCreateEditModes;
  locationId: number;
  location: LocationDto = {
    name: '',
    description: '',
    isRemoved: false,
    plz: { plz: '', ort: '' },
    state: null,
    address: '',
    size: null,
    createdAt: null,
    callerIsOwner: false,
    tags: [],
  };
  tags: string[];
  selectedTag: string;

  plzInputChange = new Subject<string>();
  plzSuggestions: Plz[] = [];

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService,
    private router: Router,
    private plzService: PlzService,
    private toastService: ToastService
  ) {}

  public get headerTitle(): string {
    return this.isEditMode === LocationCreateEditModes.EDIT ? 'Location bearbeiten' : 'Location erstellen';
  }

  public get submitButtonText(): string {
    switch (this.isEditMode) {
      case LocationCreateEditModes.CREATE:
        return 'Weiter und Bilder hochladen';
      case LocationCreateEditModes.EDIT:
        return 'Speichern';
      default:
        return '?';
    }
  }

  ngOnInit() {
    this.locationService.getAllTags().subscribe(tags => {
      this.tags = tags.tags;
    });

    this.route.data.subscribe(data => {
      this.isEditMode = data.isEditMode;
    });

    if (this.isEditMode === LocationCreateEditModes.EDIT) {
      this.route.params.subscribe(params => {
        this.locationId = params.id;

        this.locationService.getById(this.locationId).subscribe({
          next: location => {
            this.location = location;
            console.log('Location', this.location);

            for (const tag of this.location.tags) {
              this.tags = this.tags?.filter(t => t !== tag);
            }

            this.location.tags.sort();

            if (!this.location.callerIsOwner) {
              console.log('Not owner of location');
              this.router.navigate(['/location', this.locationId]);
            }
          },
          error: e => {
            this.toastService.showErrorResponse(e);
            this.router.navigate(['']);
          },
        });
      });
    }

    this.plzInputChange
      .pipe(
        debounceTime(300),
        switchMap(val => this.plzService.findSuggestions(val))
      )
      .subscribe({
        next: plzs => {
          this.plzSuggestions = plzs;
          if (plzs.length === 1 && !this.location.coord) {
            this.location.coord = plzs[0].coord;
          }
        },
        error: e => {
          this.toastService.showErrorResponse(e);
        },
      });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    if (form.valid) {
      if (this.isEditMode === LocationCreateEditModes.CREATE) {
        this.locationService.create(this.location).subscribe({
          next: location => {
            console.log('Created location', location);
            // Nagivate to /location/:id/image-edit
            this.router.navigate(['/location', location.id, 'image-edit']);
          },
          error: e => this.toastService.showErrorResponse(e),
        });
      } else {
        this.locationService.update(this.locationId, this.location).subscribe({
          next: location => {
            console.log('Updated location', location);
            this.router.navigate(['/location', location.id]);
          },
          error: e => this.toastService.showErrorResponse(e),
        });
      }
    }
  }

  public onChangeCoord(coord: LatLngLiteral) {
    this.location.coord = coord;
  }

  public onTagAdded(): void {
    if (this.selectedTag) {
      this.tags = this.tags.filter(tag => tag !== this.selectedTag);
      this.location.tags.push(this.selectedTag); //
      this.selectedTag = null;
      this.location.tags.sort();
    }
  }

  moveTagLeft($event: number) {
    const tagID = $event - 1;

    const tag = this.location.tags[tagID];
    this.location.tags[tagID] = this.location.tags[tagID - 1];
    this.location.tags[tagID - 1] = tag;
  }

  moveTagRight($event: number) {
    const tagID = $event - 1;

    const tag = this.location.tags[tagID];
    this.location.tags[tagID] = this.location.tags[tagID + 1];
    this.location.tags[tagID + 1] = tag;
  }

  /*
   * Removes the image with the given ID from the list
   * @param $event ID of the image to be removed
   */
  deleteTag($event: number) {
    const tagID = $event - 1;

    this.tags.push(this.location.tags.splice(tagID, 1)[0]);
    this.location.tags.sort();
  }
}
