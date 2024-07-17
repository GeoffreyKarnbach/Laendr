insert into app_user (id, email, name, password, is_locked, login_attempts, is_deleted)
values (-1, 'renter@email.com', 'test_renter', '$2a$10$4I3A754U07n/JxiNrX0/hudHJsb5nk9ePaFYWbK9o2JJG3ycJKqa2', true,
        0, false);

insert into reputation_renter (id, ratings, karma, weight_positive, weight_negative)
values (-3, 0, 0.5, 0, 0);

insert into renter (user_id, email, phone, reputation_id, is_deleted)
values (-1, 'renter@email.com', '+43 1231212 132', - 3, false);