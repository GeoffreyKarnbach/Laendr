insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-1, 'email@test.com', 'test_lender_1', '', false, 0, false),
       (-2, 'owner@test.com', 'test_owner_2', '', false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-10, 0, 0.5, 0, 0),
       (-11, 0, 0.5, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (-1, -10, false),
       (-2, -11, false);

insert into reputation_location (id, ratings, karma, weight_positive, weight_negative)
values (-1, 0, 0.5, 0, 0),
       (-2, 0, 0.5, 0, 0),
       (-3, 0, 0.5, 0, 0);

insert into plz(plz, ort)
values (3100, 'St. Pölten');

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id,
                      coord_lat, coord_lon)
values (-1, 'test_location_1', '', false, 3100, 'NOE', 'address_1', 100, -1, -1, 50, 50),
       (-2, 'test_location_2', 'interesting description', false, 3100, 'W', 'address_2', 200, -1, -2, 50.5, 50),
       (-3, 'test_location_3', '', false, 3100, 'OOE', 'address_3', 300, -2, -3, 60, 60);
