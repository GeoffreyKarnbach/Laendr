INSERT INTO app_user(id, email, name, password, is_locked, login_attempts, created_at, updated_at, is_deleted)
VALUES (-1, 'test1@mail.com', 'TEST_lender', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
        (-2, 'test2@mail.com', 'TEST_lender', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
        (-3, 'test3@mail.com', 'TEST_renter', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
                DATEADD(YEAR, -3, CURRENT_DATE()), false);

INSERT INTO reputation_lender(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                              updated_at)
VALUES (-1, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-2, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());

INSERT INTO reputation_renter(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                              updated_at)
VALUES (-3, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());

INSERT INTO reputation_location(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                                updated_at)
VALUES (-4, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());

INSERT INTO lender(user_id, reputation_id, created_at, updated_at, is_deleted)
VALUES (-1, -1, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-2, -2, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false);

INSERT INTO renter(user_id, reputation_id, created_at, updated_at, is_deleted)
VALUES (-3, -3, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false);

INSERT INTO location(id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id,
                     created_at, updated_at)
VALUES (-1, 'TEST_location', '', false, null, 'NOE', '1000', 999, -1, -4, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()));

INSERT INTO timeslot(id, start_at, end_at, is_used, is_deleted, created_at, updated_at, location_id)
VALUES (-1, current_timestamp, current_timestamp, true, false, current_timestamp, current_timestamp, -1);

INSERT INTO review_location(id, rating, comment, created_at, updated_at, reputation_id, reviewer_id, reviewee_karma, reviewer_karma)
VALUES (-1, 2, 'comment', current_timestamp, current_timestamp, -4, -3, 1, 1);

INSERT INTO transaction(id, initial_message, created_at, updated_at, timeslot_id, renter_id, review_location_id)
VALUES (-1, 'message', current_timestamp, current_timestamp, -1, -3, -1);