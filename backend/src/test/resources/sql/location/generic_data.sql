insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-1, 'lender@email.com', 'test_lender_2', '', false, 0, false),
       (-2, 'renter@email.com', 'test_renter_1', '', false, 0, false),
       (-3, 'renter2@email.com', 'test_renter_2', '', false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-1, 0, 0.5, 0, 0),
       (-2, 0, 0.5, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (-1, -1, false),
       (-2, -2, false);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (-3, 0, 0.5, 0, 0),
       (-4, 0, 0.5, 0, 0),
       (-5, 0, 0.5, 0, 0);

insert into renter (user_id, reputation_id, is_deleted)
values (-1, -3, false),
       (-2, -4, false),
       (-3, -5, false);

insert into reputation_location (id, ratings, karma, weight_positive, weight_negative)
values (1, 0, 0.5, 0, 0),
       (2, 0, 0.5, 0, 0);

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (1, 'test_location_1_name', 'test_location_1_description', false, null, 'W', 'test_location_1_address',
        1000.00, -1, 1);

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (2, 'test_location_2_name', 'test_location_2_description', false, null, 'NOE', 'test_location_2_address',
        2000.00, -1, 2);