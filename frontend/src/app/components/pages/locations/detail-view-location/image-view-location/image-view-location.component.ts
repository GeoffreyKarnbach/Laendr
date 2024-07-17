import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Globals } from 'src/app/global';
import { ImageService, ToastService } from 'src/app/services';

@Component({
  selector: 'app-image-view-location',
  templateUrl: './image-view-location.component.html',
  styleUrls: ['./image-view-location.component.scss'],
})
export class ImageViewLocationComponent implements OnInit {
  @ViewChild('imageModal') imageModal;
  locationId = -1;
  imageURLs: string[] = [];

  modalImageUrl: string;
  modalImageAlt: string;

  constructor(
    private route: ActivatedRoute,
    private imageService: ImageService,
    private globals: Globals,
    private toastService: ToastService,
    private modalService: NgbModal
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.locationId = params.id;

      this.imageService.getImages(this.locationId).subscribe({
        next: imageCollection => {
          for (const image of imageCollection.images) {
            this.imageURLs.push(this.globals.backendUri + '/images/' + image);
          }
        },
        error: e => this.toastService.showErrorResponse(e),
      });
    });
  }

  openImageModal(imageUrl: string): void {
    this.modalImageUrl = imageUrl;
    this.modalImageAlt = 'Image';
    const modalRef = this.modalService.open(this.imageModal, { windowClass: 'image-view-modal-content' });
    modalRef.result.then(
      result => {
        console.log(`Closed with: ${result}`);
      },
      reason => {
        console.log(`Dismissed with: ${reason}`);
      }
    );
  }
}
