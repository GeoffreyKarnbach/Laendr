import { BrowserModule } from '@angular/platform-browser';
import { LOCALE_ID, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {
  HeaderComponent,
  FooterComponent,
  HomeComponent,
  LoginComponent,
  TimeslotComponent,
  CreateEditLocationComponent,
  DetailViewLocationComponent,
  ImageViewLocationComponent,
  ImageEditComponent,
  SingleImageComponent,
  EnumBundeslandToDescription,
  RatingsViewComponent,
  RatingsDisplayComponent,
  SearchComponent,
  LenderComponent,
  SignUpComponent,
  PageableComponent,
  TransactionComponent,
  TransactionListComponent,
  TransactionItemComponent,
  NavTransactionComponent,
  ToastComponent,
  TimeslotCreateEditComponent,
  ReviewViewLocationComponent,
  CreateReviewComponent,
  CancelledTransactionsComponent,
  ReviewTransactionsComponent,
  HomeTransactionsComponent,
  HomeLocationDisplayComponent,
  MapFilterComponent,
  LocationMapComponent,
  TagComponent,
  MapPopupComponent,
  UserComponent,
  PasswordsComponent,
  UserLenderOptinComponent,
} from './components';

import { RatingToStarsPipe } from './components/shared/pipes';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { httpInterceptorProviders } from './interceptors';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
// eslint-disable-next-line max-len
import { RatingsViewDetailsComponent } from './components/pages/ratings-view/ratings-view-details/ratings-view-details.component';
import { AdminUserComponent } from './components/pages/admin-user/admin-user.component';

registerLocaleData(localeDe, 'de', localeDeExtra);

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    LoginComponent,
    TimeslotComponent,
    PageableComponent,
    SearchComponent,
    SignUpComponent,
    CreateEditLocationComponent,
    DetailViewLocationComponent,
    LenderComponent,
    ImageEditComponent,
    SingleImageComponent,
    ImageViewLocationComponent,
    EnumBundeslandToDescription,
    RatingsDisplayComponent,
    TransactionComponent,
    TransactionListComponent,
    TransactionItemComponent,
    NavTransactionComponent,
    RatingsViewComponent,
    TagComponent,
    ToastComponent,
    TimeslotCreateEditComponent,
    ReviewViewLocationComponent,
    RatingToStarsPipe,
    LocationMapComponent,
    CreateReviewComponent,
    MapFilterComponent,
    RatingsViewDetailsComponent,
    CancelledTransactionsComponent,
    HomeTransactionsComponent,
    HomeLocationDisplayComponent,
    MapPopupComponent,
    UserComponent,
    PasswordsComponent,
    UserLenderOptinComponent,
    AdminUserComponent,
    ReviewTransactionsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgbModule,
    FormsModule,
    LeafletModule,
  ],
  providers: [{ provide: LOCALE_ID, useValue: 'de-AT' }, httpInterceptorProviders],
  bootstrap: [AppComponent],
})
export class AppModule {}
