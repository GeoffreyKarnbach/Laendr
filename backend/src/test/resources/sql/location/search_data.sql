insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (1, '', 'test_lender_1', '', false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (1, 0, 0.5, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (1, 1, false);

insert into reputation_location (id, ratings, karma, weight_positive, weight_negative)
values (2, 0, 0.5, 0, 0);

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (1, 'test_location_1', '', false, null, 'NOE', '', 0, 1, 2);

