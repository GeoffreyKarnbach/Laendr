import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-tag',
  templateUrl: './tag.component.html',
  styleUrls: ['./tag.component.scss'],
})
export class TagComponent {
  @Input() tagname: string;
  @Input() isEditable: false;
  @Input() position: number;
  @Input() totalTagNumber: number;

  @Output() moveTagLeftEvent = new EventEmitter<number>();
  @Output() moveTagRightEvent = new EventEmitter<number>();
  @Output() deleteTagEvent = new EventEmitter<number>();

  moveTagLeft() {
    this.moveTagLeftEvent.emit(this.position);
  }

  moveTagRight() {
    this.moveTagRightEvent.emit(this.position);
  }

  deleteTag() {
    this.deleteTagEvent.emit(this.position);
  }
}
