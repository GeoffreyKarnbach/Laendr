SET
REFERENTIAL_INTEGRITY FALSE;

DELETE
FROM app_admin;
DELETE
FROM product_image;
DELETE
FROM lender;
DELETE
FROM location;
DELETE
FROM location_tag;
DELETE
FROM location_to_location_tag;
DELETE
FROM renter;
DELETE
FROM reputation_lender;
DELETE
FROM reputation_location;
DELETE
FROM reputation_renter;
DELETE
FROM review_location;
DELETE
FROM review_renter;
DELETE
FROM timeslot;
DELETE
FROM transaction;
DELETE
FROM app_user;
DELETE
FROM plz;

alter sequence seq_user restart with 100;
alter sequence seq_product_image restart with 100;
alter sequence seq_location restart with 100;
alter sequence seq_product_tag restart with 100;
alter sequence seq_reputation restart with 100;
alter sequence seq_review restart with 100;
alter sequence seq_timeslot restart with 100;
alter sequence seq_transaction restart with 100;

SET
REFERENTIAL_INTEGRITY TRUE;