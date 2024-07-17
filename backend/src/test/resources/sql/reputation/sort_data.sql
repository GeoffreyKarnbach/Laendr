INSERT INTO app_user(id, email, name, password, is_locked, login_attempts, is_deleted) VALUES
  (-1, 'lender@email.com', 'Lender', '', false, 0, false);

INSERT INTO reputation_lender(id, ratings, karma, weight_positive, weight_negative) VALUES
  (-1, 0, 0.5, 0, 0);

INSERT INTO lender(user_id, reputation_id, is_deleted) VALUES
  (-1, -1, false);

INSERT INTO reputation_location(id, ratings, karma, weight_positive, weight_negative) VALUES
  (-2, 0, 0.9, 0, 0),
  (-3, 0, 0.8, 0, 0),
  (-4, 0, 0.7, 0, 0);

INSERT INTO location(id, name, description, is_removed, plz, state, address, size_in_m2, lender_id, reputation_id) VALUES
  (-1, 'Location1', '', false, null, 'NOE', '', 1, -1, -2),
  (-2, 'Location2', '', false, null, 'NOE', '', 1, -1, -3),
  (-3, 'Location3', '', false, null, 'NOE', '', 1, -1, -4);
