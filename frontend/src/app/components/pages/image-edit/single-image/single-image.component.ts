import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-single-image',
  templateUrl: './single-image.component.html',
  styleUrls: ['./single-image.component.scss'],
})
export class SingleImageComponent {
  @Input() imagePosition: number;
  @Input() imageUrl = '';
  @Input() totalImageNumber: number;

  @Output() moveImageLeftEvent = new EventEmitter<number>();
  @Output() moveImageRightEvent = new EventEmitter<number>();
  @Output() deleteImageEvent = new EventEmitter<number>();

  moveImageLeft() {
    this.moveImageLeftEvent.emit(this.imagePosition);
  }

  moveImageRight() {
    this.moveImageRightEvent.emit(this.imagePosition);
  }

  deleteImage() {
    this.deleteImageEvent.emit(this.imagePosition);
  }

  getPrimaryStyle(): string {
    if (this.imagePosition === 1) {
      return 'primary_image';
    }
    return 'secondary_image';
  }
}
