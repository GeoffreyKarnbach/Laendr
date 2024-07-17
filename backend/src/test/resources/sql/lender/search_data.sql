insert into app_user (id, email, name, password, plz, is_locked, login_attempts, is_deleted)
values (-1, 'lender@email.com', 'test_lender_1', '', null, false, 0, false),
       (-2, 'lender2@email.com', 'test_lender_2', '', null, false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-1, 0, 0, 0, 0),
       (-2, 0, 0, 0, 0);

insert into lender (user_id, reputation_id, phone, description, email, is_deleted)
values (-1, -1, 123456, 'desc1', 'email@email.com', false),
       (-2, -2, 147258, 'deso', 'gmail@outlook.info', false);
