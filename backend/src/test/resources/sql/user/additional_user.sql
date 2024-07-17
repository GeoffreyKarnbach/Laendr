insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-3, 'renter2@email.com', 'test_renter_2', '', false, 0, false);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (-3, 0, 0.5, 0, 0);

insert into renter (user_id, reputation_id, is_deleted)
values (-3, -3, false);