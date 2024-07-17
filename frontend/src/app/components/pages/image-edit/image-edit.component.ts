import { Component, Input, OnInit } from '@angular/core';
import { ImageService, ToastService } from 'src/app/services';
import { Globals } from 'src/app/global';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-image-edit',
  templateUrl: './image-edit.component.html',
  styleUrls: ['./image-edit.component.scss'],
})
export class ImageEditComponent implements OnInit {
  @Input() locationId = 10;
  imageURLs: string[] = [];
  imageFiles: File[] = [];
  imageCounter = 0;
  maxFileSize = 1000000;
  isEdit = false;
  originalImageCount = 0;

  constructor(
    private imageService: ImageService,
    private globals: Globals,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.locationId = params.id;

      this.imageService.getImages(this.locationId).subscribe({
        next: data => {
          if (data.images.length !== 0) {
            this.isEdit = true;
            this.originalImageCount = data.images.length;
          }

          if (data.callerIsOwner === false) {
            this.toastService.showError('Sie dürfen diese Location nicht bearbeiten.');
            this.router.navigate(['/location', this.locationId]);
          }

          for (const image of data.images) {
            this.imageURLs.push(this.globals.backendUri + '/images/' + image);
            this.imageFiles.push(null);
            this.imageCounter += 1;
          }
        },
        error: e => {
          console.log(e);
          this.toastService.showErrorResponse(e);
        },
      });
    });
  }

  /*
   * Opens the file selection dialog
   */
  selectImage() {
    document.getElementById('upload-file').click();
  }

  /*
   * Adds the selected image to the list of images
   * If the image is already in the list, it will not be added
   * If the image file size is above 5MB, it will not be added
   * There can be at most 10 images in the list
   *
   * @param event Event containing the selected image
   */
  onFileSelected(event: any) {
    if (event.target.files[0]) {
      const reader = new FileReader();
      reader.readAsDataURL(event.target.files[0]);
      reader.onload = done => {
        for (const image of this.imageURLs) {
          if (image === done.target.result) {
            this.toastService.showError('Bild bereits hochgeladen');
            return;
          }
        }

        if (event.target.files[0].size > this.maxFileSize) {
          this.toastService.showError('Bild zu groß');
          return;
        }

        if (this.imageCounter >= 10) {
          this.toastService.showError('Maximal 10 Bilder sind erlaubt');
          return;
        }

        this.imageFiles.push(event.target.files[0] as File);
        this.imageURLs.push(done.target.result as string);
        this.imageCounter += 1;

        // Reset file input
        const fileInput = document.getElementById('upload-file') as HTMLInputElement;
        fileInput.value = null;
      };
    }
  }

  /*
   * Moves the image with the given ID one position to the left
   * @param $event ID of the image to be moved
   */
  moveImageLeft($event: number) {
    const imgID = $event - 1;

    const img = this.imageURLs[imgID];
    this.imageURLs[imgID] = this.imageURLs[imgID - 1];
    this.imageURLs[imgID - 1] = img;

    const file = this.imageFiles[imgID];
    this.imageFiles[imgID] = this.imageFiles[imgID - 1];
    this.imageFiles[imgID - 1] = file;
  }

  /*
   * Moves the image with the given ID one position to the right
   * @param $event ID of the image to be moved
   */
  moveImageRight($event: number) {
    const imgID = $event - 1;

    const img = this.imageURLs[imgID];
    this.imageURLs[imgID] = this.imageURLs[imgID + 1];
    this.imageURLs[imgID + 1] = img;

    const file = this.imageFiles[imgID];
    this.imageFiles[imgID] = this.imageFiles[imgID + 1];
    this.imageFiles[imgID + 1] = file;
  }

  /*
   * Removes the image with the given ID from the list
   * @param $event ID of the image to be removed
   */
  deleteImage($event: number) {
    const imgID = $event - 1;

    this.imageURLs.splice(imgID, 1);
    this.imageFiles.splice(imgID, 1);
    this.imageCounter -= 1;
  }

  /*
   * Upload all images to the backend
   */
  uploadFinished() {
    if (this.imageFiles.length !== 0 || this.originalImageCount !== 0) {
      if (!this.isEdit) {
        this.imageService.uploadImages(this.imageFiles, this.locationId).subscribe(data => {
          console.log(data);
          this.router.navigate(['/location', this.locationId]);
        });
      } else {
        this.imageService.updateImages(this.imageURLs, this.locationId).subscribe(data => {
          console.log(data);
          this.router.navigate(['/location', this.locationId]);
        });
      }
    } else {
      this.router.navigate(['/location', this.locationId]);
    }
  }
}
