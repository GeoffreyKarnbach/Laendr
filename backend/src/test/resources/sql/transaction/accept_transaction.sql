insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (1, 'lender@email.com', 'test_lender_1', '', false, 0, false),
       (2, 'renter@email.com', 'test_renter_1', '', false, 0, false),
       (3, 'renter2@email.com', 'test_renter_2', '', false, 0, false),
       (4, 'renter3@email.com', 'test_renter_3', '', false, 0, false),
       (5, 'renter4@email.com', 'test_renter_4', '', false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (1, 0, 0, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (1, 1, false);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (2, 0, 0, 0, 0),
       (3, 0, 0, 0, 0),
       (4, 0, 0, 0, 0),
       (5, 0, 0, 0, 0),
       (6, 0, 0, 0, 0);

insert into renter (user_id, reputation_id, is_deleted)
values (1, 2, false),
       (2, 3, false),
       (3, 4, false),
       (4, 5, false),
       (5, 6, false);

insert into reputation_location (id, ratings, karma, weight_positive, weight_negative)
values (7, 0, 0, 0, 0);

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (1, 'test_location_1', '', false, null, 'NOE', '', 0, 1, 7);

insert into timeslot (id, start_at, end_at, is_used, is_deleted, location_id)
values (1, current_timestamp, current_timestamp, false, false, 1);

insert into public.transaction (id, initial_message, timeslot_id, renter_id)
values (1, 'RENTER1', 1, 2),
       (2, 'RENTER2', 1, 3),
       (3, 'RENTER3', 1, 4),
       (4, 'RENTER4', 1, 5);
