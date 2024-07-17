INSERT INTO app_user(id, email, name, password, is_locked, login_attempts, is_deleted) VALUES
  (-1, 'deleteme@email.local', 'Delete Me', '', false, 0, false),
  (-2, 'otheruser@email.local', 'Other User', '', false, 0, false);

INSERT INTO reputation_renter(id, ratings, karma, weight_positive, weight_negative) VALUES
  (-1, 0, 0.5, 0, 0),
  (-2, 0, 0.5, 0, 0);
INSERT INTO renter(user_id, reputation_id, is_deleted, phone, email) VALUES
  (-1, -1, false, '+12 34 56 78 90', 'deleteme@email.local'),
  (-2, -2, false, '+01 23 45 67 89', 'otheruser@email.local');

INSERT INTO reputation_lender(id, ratings, karma, weight_positive, weight_negative) VALUES
  (-3, 0, 0.5, 0, 0),
  (-4, 0, 0.5, 0, 0);
INSERT INTO lender(user_id, reputation_id, is_deleted, phone, email, description) VALUES
  (-1, -3, false, '+12 34 56 78 90', 'deleteme@email.local', 'I lend and will be deleted'),
  (-2, -4, false, '+01 23 45 67 89', 'otheruser@email.local', 'I lend and will not be deleted');

INSERT INTO reputation_location(id, ratings, karma, weight_positive, weight_negative) VALUES
  (-5, 0, 0.5, 0, 0),
  (-6, 0, 0.5, 0, 0);
INSERT INTO location(id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id) VALUES
  (-1, 'Delete me location', 'Please delete this', false, null, 'W', 'Locationstrasse 1', 123, -1, -5),
  (-2, 'Other location', 'Do not delete please', false, null, 'W', 'Locationstrasse 2', 456, -2, -6);
INSERT INTO timeslot(id, start_at, end_at, price, price_hourly, is_used, is_deleted, location_id) VALUES
  (-1, '2010-10-10T12:00:00', '2010-10-10T13:00', 100, 100, true, false, -1),
  (-2, '2010-10-10T12:00:00', '2010-10-10T13:00', 100, 100, true, false, -2);

INSERT INTO transaction(id, renter_id, initial_message, is_cancelled, timeslot_id) VALUES
  (-1, -1, 'I will be cancelled', false, -2),
  (-2, -2, 'I will be cancelled', false, -1);
