INSERT INTO app_user(id, email, name, password, is_locked, login_attempts, created_at, updated_at, is_deleted)
VALUES (-1, 'test1@mail.com', 'TEST_lender', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-2, 'test2@mail.com', 'TEST_renter', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-3, 'test3@mail.com', 'TEST_lenderOldReputation', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-6, 'test4@mail.com', 'TEST_2lender', '', false, 0, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-8, 'test5@mail.com', 'TEST_lenderYoung', '', false, 0, DATEADD(HOUR, -1, CURRENT_DATE()),
        DATEADD(YEAR, -1, CURRENT_DATE()), false);

INSERT INTO reputation_lender(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                              updated_at)
VALUES (-1, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-7, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-9, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       -- older reputations karma should become more neutral when applying time decay
       (-4, 2, 0, 0.714, 4, 1, DATEADD(MONTH, -1, CURRENT_DATE()), DATEADD(MONTH, -1, CURRENT_DATE()));
INSERT INTO reputation_renter(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                              updated_at)
VALUES (-2, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());
INSERT INTO reputation_location(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                                updated_at)
VALUES (-3, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-10, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-11, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-12, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());

INSERT INTO lender(user_id, reputation_id, created_at, updated_at, is_deleted)
VALUES (-1, -1, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-3, -4, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-6, -7, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false),
       (-8, -9, DATEADD(HOUR, -1, CURRENT_DATE()), DATEADD(HOUR, -1, CURRENT_DATE()), false);

INSERT INTO renter(user_id, reputation_id, created_at, updated_at, is_deleted)
VALUES (-2, -2, DATEADD(YEAR, -3, CURRENT_DATE()), DATEADD(YEAR, -3, CURRENT_DATE()), false);

INSERT INTO location(id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id,
                     created_at, updated_at)
VALUES (-1, 'TEST_location', '', false, null, 'NOE', '', 999, -1, -3, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE())),
       (-2, 'TEST_2location', '', false, null, 'NOE', '', 999, -6, -10, DATEADD(YEAR, -3, CURRENT_DATE()),
        DATEADD(YEAR, -3, CURRENT_DATE())),
       (-3, 'TEST_3location', '', false, null, 'NOE', '', 999, -1, -11, DATEADD(HOUR, -1, CURRENT_DATE()),
        DATEADD(HOUR, -1, CURRENT_DATE())),
       (-4, 'TEST_3location', '', false, null, 'NOE', '', 999, -8, -12, DATEADD(HOUR, -3, CURRENT_DATE()),
        DATEADD(HOUR, -3, CURRENT_DATE()));

INSERT INTO app_user(id, email, name, password, is_locked, login_attempts, is_deleted)
VALUES (-4, 'test6@mail.com', 'TEST_mocking_renter', '', false, 0, false),
       (-5, 'test7@mail.com', 'TEST_mocking_renter2', '', false, 0, false),
       (-7, 'test8@mail.com', 'TEST_mocking_renterYoung', '', false, 0, false);
INSERT INTO reputation_renter(id, average_rating, ratings, karma, weight_positive, weight_negative, created_at,
                              updated_at)
VALUES (-5, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-6, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE()),
       (-8, 2, 0, 0.5, 0, 0, CURRENT_DATE(), CURRENT_DATE());
INSERT INTO renter(user_id, reputation_id, created_at, updated_at, is_deleted)
VALUES (-4, -5, DATEADD(MONTH, -12, CURRENT_DATE()), DATEADD(MONTH, -12, CURRENT_DATE()), false),
       (-5, -6, DATEADD(MONTH, -12, CURRENT_DATE()), DATEADD(MONTH, -12, CURRENT_DATE()), false),
       (-7, -8, DATEADD(HOUR, -1, CURRENT_DATE()), DATEADD(HOUR, -1, CURRENT_DATE()), false);
