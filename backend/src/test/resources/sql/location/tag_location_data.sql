insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-1, 'email@test.com', 'test_lender_1', '', false, 0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-10, 0, 0.5, 0, 0);

insert into lender (user_id, reputation_id, is_deleted)
values (-1, -10, false);

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
       (-3, 'test_location_3', '', false, 3103, 'OOE', 'address_3', 300, -1, -3);

insert into LOCATION_TAG (ID, NAME)
values (-1, 'Tag 1'),
       (-2, 'Tag 2'),
       (-3, 'Tag 3');

insert into LOCATION_TO_LOCATION_TAG (LOCATION_ID, LOCATION_TAG_ID)
values (-1, -1),
       (-1, -2),
       (-2, -2),
       (-2, -3),
       (-3, -1),
       (-3, -2);
//,(-3, -3);
