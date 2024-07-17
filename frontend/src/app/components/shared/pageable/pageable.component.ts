import { Component, ContentChild, EventEmitter, Input, Output, TemplateRef } from '@angular/core';

@Component({
  selector: 'app-pageable',
  templateUrl: './pageable.component.html',
  styleUrls: ['./pageable.component.scss'],
})
export class PageableComponent {
  @Input() collectionSize: number;
  @ContentChild(TemplateRef) templateRef: TemplateRef<number>;
  @Output() pageChange?: EventEmitter<[page: number, pageSize: number]> = new EventEmitter();
  @Input() page = 1;

  @Input() pageSize = 5;

  onPageChange(newPage: number) {
    this.page = newPage;
    this.pageChange.emit([this.page, this.pageSize]);
  }

  onPageSizeChange(newPageSize: number) {
    this.pageSize = newPageSize;
    this.pageChange.emit([this.page, this.pageSize]);
  }
}
