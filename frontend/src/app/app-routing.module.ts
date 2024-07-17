import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  CreateEditLocationComponent,
  HomeComponent,
  LoginComponent,
  SearchComponent,
  DetailViewLocationComponent,
  LocationCreateEditModes,
  ImageEditComponent,
  TransactionComponent,
  TransactionListComponent,
  SignUpComponent,
  LenderComponent,
  RatingsViewComponent,
  RatingsViewDetailsComponent,
  RatingsViewDetailsModes,
  UserComponent,
  AdminUserComponent,
} from './components';

import { AuthGuard } from './guards';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignUpComponent },
  {
    path: 'user',
    canActivate: [AuthGuard],
    children: [{ path: ':id', component: UserComponent }],
  },
  { path: 'search', component: SearchComponent },
  {
    path: 'transaction',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: TransactionListComponent },
      { path: ':id', component: TransactionComponent },
    ],
  },
  { path: 'lender/:id', component: LenderComponent },
  {
    path: 'location',
    children: [
      { path: 'create', component: CreateEditLocationComponent, data: { isEditMode: LocationCreateEditModes.CREATE } },
      { path: ':id/edit', component: CreateEditLocationComponent, data: { isEditMode: LocationCreateEditModes.EDIT } },
      { path: ':id', component: DetailViewLocationComponent },
      { path: ':id/image-edit', canActivate: [AuthGuard], component: ImageEditComponent },
    ],
  },
  {
    path: 'ratings',
    children: [
      { path: '', component: RatingsViewComponent },
      {
        path: 'details/lender/:id',
        component: RatingsViewDetailsComponent,
        data: { mode: RatingsViewDetailsModes.LENDER },
      },
      {
        path: 'details/location/:id',
        component: RatingsViewDetailsComponent,
        data: { mode: RatingsViewDetailsModes.LOCATION },
      },
      {
        path: 'details/renter/:id',
        component: RatingsViewDetailsComponent,
        data: { mode: RatingsViewDetailsModes.RENTER },
      },
    ],
  },
  { path: 'admin/user', component: AdminUserComponent, canActivate: [AuthGuard] },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
