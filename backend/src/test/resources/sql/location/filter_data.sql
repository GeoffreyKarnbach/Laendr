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
values (3101, 'Wien1'),
       (3102, 'Wien2'),
       (3103, 'Wien3');

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (-1, 'test_location_1', '', false, 3101, 'NOE', 'address_1', 100, -1, -1),
       (-2, 'test_location_2', 'interesting description', false, 3102, 'W', 'address_2', 200, -1, -2),
       (-3, 'test_location_3', '', false, 3103, 'OOE', 'address_3', 300, -2, -3);

insert into timeslot (id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id)
values (-1, '3021-05-13T14:00:00', '3021-05-13T16:00:00', 600, 300, false, false, -1);
insert into timeslot (id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id)
values (-2, '3022-05-13T14:00:00', '3022-05-13T18:00:00', 120, 60, false, false, -1);
insert into timeslot (id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id)
values (-3, '3023-05-13T16:00:00', '3023-05-13T20:00:00', 600, 300, true, false, -2);
insert into timeslot (id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id)
values (-4, '3024-05-13T17:00:00', '3024-05-13T20:00:00', 600, 300, false, false, -2);
