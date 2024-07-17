insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-1, 'lender@email.com', 'test_lender_2', '$2a$10$4I3A754U07n/JxiNrX0/hudHJsb5nk9ePaFYWbK9o2JJG3ycJKqa2', false,
        0, false),
       (-2, 'renter@email.com', 'test_renter_1', '$2a$10$4I3A754U07n/JxiNrX0/hudHJsb5nk9ePaFYWbK9o2JJG3ycJKqa2', false,
        0, false),
       (-3, 'renter2@email.com', 'test_renter_2', '$2a$10$4I3A754U07n/JxiNrX0/hudHJsb5nk9ePaFYWbK9o2JJG3ycJKqa2', false,
        0, false);

insert into reputation_lender (id, ratings, karma, weight_positive, weight_negative)
values (-1, 0, 0.5, 0, 0),
       (-2, 0, 0.5, 0, 0);

insert into lender (user_id, email, phone, description, reputation_id, is_deleted)
values (-1, 'lender@email.com', '0660 546045', 'Beschreibung 1', -1, false),
       (-2, '', '', '', -2, false);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (-3, 0, 0.5, 0, 0),
       (-4, 0, 0.5, 0, 0),
       (-5, 0, 0.5, 0, 0);

insert into renter (user_id, email, phone, reputation_id, is_deleted)
values (-1, 'renter@email.com', '+43 1231212 132', - 3, false),
       (-2, '', '', -4, false),
       (-3, '', '', -5, false);