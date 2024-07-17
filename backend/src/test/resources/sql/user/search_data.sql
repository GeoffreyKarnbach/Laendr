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