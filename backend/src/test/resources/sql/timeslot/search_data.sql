insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-2, 'test_lender@email.com', 'test_lender_1', '', false, 0, false),
       (-4, 'test_lender2@email.com', 'test_lender_2', '', false, 0, false),
       (-3, 'test_renter@email.com', 'test_renter_1', '', false, 0, false);


insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-2, 0, 0.5, 0, 0),
       (-4, 0, 0.5, 0, 0);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (-3, 0, 0.5, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (-2, -2, false),
       (-4, -4, false);

insert into renter (user_id, reputation_id, is_deleted)
values (-3, -3, false);

insert into reputation_location (id, ratings, karma, weight_positive, weight_negative)
values (-5, 0, 0.5, 0, 0);

insert into location (id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id)
values (-2, 'test_location_1', '', false, null, 'NOE', '', 0, -2, -5);

insert into timeslot (id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id)
values (-1, '2090-05-13T14:00:00', '2090-05-13T16:00:00', 100, 50, false, false, -2),
       (-2, '2005-05-13T14:00:00', '2090-05-13T18:00:00', 100, 50, false, false, -2),
       (-3, '2090-05-13T16:00:00', '2090-05-13T20:00:00', 100, 50, true, false, -2),
       (-4, '2090-05-13T17:00:00', '2090-05-13T20:00:00', 100, 50, false, false, -2);

insert into transaction (id, renter_id, initial_message,  is_cancelled, timeslot_id)
values (-1, -3,'init message', false, -1);
