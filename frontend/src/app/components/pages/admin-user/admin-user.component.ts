import { Component, OnInit } from '@angular/core';
import { AuthService, UserService } from 'src/app/services';
import { Router } from '@angular/router';
import { Role } from 'src/app/enum';
import { UserDto } from 'src/app/dtos';

@Component({
  selector: 'app-admin-user',
  templateUrl: './admin-user.component.html',
  styleUrls: ['./admin-user.component.scss'],
})
export class AdminUserComponent implements OnInit {
  allUsers: UserDto[] = [];

  displayOnlyLocked = true;

  page = 0;
  pageSize = 10;
  totalResults = 0;

  constructor(private authService: AuthService, private router: Router, private userService: UserService) {}

  ngOnInit() {
    if (!this.authService.getUserRole().includes(Role.ROLE_ADMIN)) {
      this.router.navigate(['/']);
    } else {
      this.fetchUsers();
    }
  }

  fetchUsers() {
    this.userService.getAllUsers(this.displayOnlyLocked, this.page, this.pageSize).subscribe(users => {
      this.totalResults = users.totalResults;
      this.allUsers = users.result;
    });
  }

  onPageChange(event: [number, number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.fetchUsers();
  }

  unlockUser(userId: number) {
    this.userService.unlockUser(userId).subscribe(() => {
      this.fetchUsers();
    });
  }

  lockUser(userId: number) {
    this.userService.lockUser(userId).subscribe(() => {
      this.fetchUsers();
    });
  }

  onDisplayOnlyLockedChange(event) {
    this.displayOnlyLocked = event.target.checked;
    this.fetchUsers();
  }
}
