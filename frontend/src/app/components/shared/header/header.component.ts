import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Role } from 'src/app/enum';
import { AuthService } from 'src/app/services';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {
  public searchString = '';

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit() {}

  onClickSearch() {
    this.router.navigate(['/search'], { queryParams: { q: this.searchString } });
  }

  isLender(): boolean {
    return this.authService.isLoggedIn() && this.authService.getUserRole().includes(Role.ROLE_LENDER);
  }

  isAdmin(): boolean {
    return this.authService.isLoggedIn() && this.authService.getUserRole().includes(Role.ROLE_ADMIN);
  }
}
