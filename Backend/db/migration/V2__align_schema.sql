-- ============================================================================
-- V2: Align the legacy MySQL schema with the current JPA entity model.
--
-- Fixes:
--   1. booking: remove legacy columns/constraints, add missing `booking_date`.
--   2. passenger: drop legacy unmapped columns (first_name/type/... NOT NULL
--      without defaults were breaking inserts).
--   3. invoice: drop the stray FK to users (customer_id must only point at
--      customer) and the legacy NOT NULL `payment_status` column.
--   4. tour: give the unmapped NOT NULL `reviews` column a default so
--      POST /api/tours and the Excel upload stop failing.
--   5. tour / tour_schedule deletes: switch child FK constraints to
--      ON DELETE CASCADE so DELETE /api/tours/{id} and DELETE schedules work.
--
-- Idempotent: every statement is guarded by information_schema checks so this
-- script can safely be re-run.
--
-- Run manually (backup first if this is a real environment):
--   mysql -u root -p etour < db/migration/V2__align_schema.sql
-- ============================================================================

USE etour;

-- ---------------------------------------------------------------------------
-- Clean up corrupt legacy data BEFORE touching schema.
-- The old booking row points at customer_id=0 / schedule_id=0 which do not
-- exist, so FK creation below would otherwise fail. Remove its dependents
-- first (they are stale test data attached to the corrupt booking).
-- ---------------------------------------------------------------------------
DELETE p FROM passenger p
JOIN booking b ON p.booking_id = b.booking_id
WHERE b.customer_id = 0 OR b.schedule_id = 0;

DELETE pm FROM payment pm
JOIN booking b ON pm.booking_id = b.booking_id
WHERE b.customer_id = 0 OR b.schedule_id = 0;

DELETE i FROM invoice i
JOIN booking b ON i.booking_id = b.booking_id
WHERE b.customer_id = 0 OR b.schedule_id = 0;

DELETE ba FROM booking_addon ba
JOIN booking b ON ba.booking_id = b.booking_id
WHERE b.customer_id = 0 OR b.schedule_id = 0;

DELETE bo FROM booking_add_on bo
JOIN booking b ON bo.booking_id = b.booking_id
WHERE b.customer_id = 0 OR b.schedule_id = 0;

DELETE FROM booking WHERE customer_id = 0 OR schedule_id = 0;

-- ---------------------------------------------------------------------------
DELIMITER $$

-- Drop a foreign key constraint if it exists.
DROP PROCEDURE IF EXISTS ensure_drop_fk $$
CREATE PROCEDURE ensure_drop_fk(IN tbl VARCHAR(64), IN fk VARCHAR(64))
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
             WHERE CONSTRAINT_SCHEMA = DATABASE()
               AND TABLE_NAME = tbl
               AND CONSTRAINT_NAME = fk
               AND CONSTRAINT_TYPE = 'FOREIGN KEY') THEN
    SET @s = CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk, '`');
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END $$

-- Drop a column if it exists.
DROP PROCEDURE IF EXISTS ensure_drop_column $$
CREATE PROCEDURE ensure_drop_column(IN tbl VARCHAR(64), IN col VARCHAR(64))
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = tbl
               AND COLUMN_NAME = col) THEN
    SET @s = CONCAT('ALTER TABLE `', tbl, '` DROP COLUMN `', col, '`');
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END $$

-- Add a column if it does not exist.
DROP PROCEDURE IF EXISTS ensure_add_column $$
CREATE PROCEDURE ensure_add_column(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl VARCHAR(512))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = tbl
                   AND COLUMN_NAME = col) THEN
    SET @s = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', ddl);
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END $$

-- Re-point a child FK to ON DELETE CASCADE (drop + re-add, same name).
DROP PROCEDURE IF EXISTS set_fk_cascade $$
CREATE PROCEDURE set_fk_cascade(IN tbl VARCHAR(64), IN fk VARCHAR(64),
                                IN col VARCHAR(64), IN ref_tbl VARCHAR(64),
                                IN ref_col VARCHAR(64))
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
             WHERE CONSTRAINT_SCHEMA = DATABASE()
               AND TABLE_NAME = tbl AND CONSTRAINT_NAME = fk
               AND CONSTRAINT_TYPE = 'FOREIGN KEY') THEN
    SET @s = CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk, '`');
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
  SET @s = CONCAT('ALTER TABLE `', tbl, '` ADD CONSTRAINT `', fk,
                  '` FOREIGN KEY (`', col, '`) REFERENCES `', ref_tbl,
                  '` (`', ref_col, '`) ON DELETE CASCADE');
  PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END $$

DELIMITER ;

-- ---------------------------------------------------------------------------
-- 1) BOOKING: match the Booking entity (booking_id, customer_id, schedule_id,
--    booking_date, total_amount, booking_status).
-- ---------------------------------------------------------------------------
CALL ensure_drop_fk('booking', 'FK7udbel7q86k041591kj6lfmvw');  -- user_id -> users (legacy)
CALL ensure_drop_fk('booking', 'FKlc7bhi14w8e558dt15eofelm4');  -- tour_id -> tour (legacy)

CALL ensure_drop_column('booking', 'adults');
CALL ensure_drop_column('booking', 'children');
CALL ensure_drop_column('booking', 'contact_first_name');
CALL ensure_drop_column('booking', 'contact_last_name');
CALL ensure_drop_column('booking', 'created_at');
CALL ensure_drop_column('booking', 'departure_date');
CALL ensure_drop_column('booking', 'email');
CALL ensure_drop_column('booking', 'extra_beds');
CALL ensure_drop_column('booking', 'phone');
CALL ensure_drop_column('booking', 'status');
CALL ensure_drop_column('booking', 'travel_choice');
CALL ensure_drop_column('booking', 'tour_id');
CALL ensure_drop_column('booking', 'user_id');
CALL ensure_drop_column('booking', 'order_number');

CALL ensure_add_column('booking', 'booking_date', "date NOT NULL");

-- ---------------------------------------------------------------------------
-- 2) PASSENGER: match the Passenger entity.
-- ---------------------------------------------------------------------------
CALL ensure_drop_column('passenger', 'age');
CALL ensure_drop_column('passenger', 'extra_bed');
CALL ensure_drop_column('passenger', 'extra_bed_charge');
CALL ensure_drop_column('passenger', 'first_name');
CALL ensure_drop_column('passenger', 'last_name');
CALL ensure_drop_column('passenger', 'type');
CALL ensure_drop_column('passenger', 'amount');
CALL ensure_drop_column('passenger', 'date_of_birth');

-- ---------------------------------------------------------------------------
-- 3) INVOICE: drop stray users FK + legacy NOT NULL column.
-- ---------------------------------------------------------------------------
CALL ensure_drop_fk('invoice', 'FKa3yvl1brgwhdutn120fkdiyaa');  -- customer_id -> users (wrong)
CALL ensure_drop_column('invoice', 'payment_status');

-- ---------------------------------------------------------------------------
-- 4) TOUR: unmapped NOT NULL `reviews` column -> give it a default.
-- ---------------------------------------------------------------------------
UPDATE tour SET reviews = 0 WHERE reviews IS NULL;
ALTER TABLE tour MODIFY reviews int NOT NULL DEFAULT 0;

-- ---------------------------------------------------------------------------
-- 5) DELETE CASCADES.
--    Child content owned by a tour/schedule is removed with its parent.
-- ---------------------------------------------------------------------------
CALL set_fk_cascade('booking_addon',    'FK1fh6ir112y8f9v2vq8g5wwo7b', 'booking_id',  'booking',       'booking_id');
CALL set_fk_cascade('booking_addon',    'FKpdxh8ius6cmxp2wgyhkq6dqyn', 'addon_id',    'tour_addon',    'addon_id');
CALL set_fk_cascade('cart',             'FKsl0f1ty8ga0gmsr411340mqid', 'schedule_id', 'tour_schedule', 'schedule_id');
CALL set_fk_cascade('cart_addon',       'FKl9dpqsriigeye7vkpjabcqg59', 'cart_id',     'cart',          'cart_id');
CALL set_fk_cascade('cart_addon',       'FKmxtl3dgs9lbncyl5fpcx5yto1', 'addon_id',    'tour_addon',    'addon_id');
CALL set_fk_cascade('invoice',          'FK4jd6uuk7w0d72riyre2w14fl7', 'booking_id',  'booking',       'booking_id');
CALL set_fk_cascade('invoice',          'FKbaxa82hce5x7dqj0sotnc1cxf', 'payment_id',  'payment',       'payment_id');
CALL set_fk_cascade('itinerary',        'FK412aybnynrvmjtt4aknad4q6l', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('journey_detail',   'FKhptr5j1whsekhw764m6008haq', 'to_location_id',   'location', 'location_id');
CALL set_fk_cascade('journey_detail',   'FKlg3akvuffc8jgd6bnkxhvaner', 'from_location_id', 'location', 'location_id');
CALL set_fk_cascade('journey_detail',   'FKnqw071gc7deutnnfvlrevv5cv', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('passenger',        'FKtco0omesfld1qi5sw76eomvt4', 'booking_id',  'booking',       'booking_id');
CALL set_fk_cascade('payment',          'FKqewrl4xrv9eiad6eab3aoja65', 'booking_id',  'booking',       'booking_id');
CALL set_fk_cascade('review',           'FK2yxuruefnrj0xan64vi2gg7ag', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('stay_meal',        'FKas3f6umr6s8p5bcmnbeiub4mq', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('stay_meal',        'FKb7mfgyp3gvb9oxh50xeiv56n6', 'location_id', 'location',      'location_id');
CALL set_fk_cascade('tour_addon',       'FKqaawi39yew35ncpl2grv1ha6g', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_category',    'FKdy6eldt453mi4tyv4gc1np7ag', 'category_id', 'category',      'category_id');
CALL set_fk_cascade('tour_category',    'FKk99gxgifscof3vuga1hvannk2', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_content',     'FKbrpyfe787p7piwqrxt0qlcyva', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_cost',        'FK65xgv1yxuyj2479iuwg38olhw', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_details',     'FK27g89or55p5ovep89frmbyva2', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_media',       'FKtrh5da8ihlear0m2phcdc5tdo', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_schedule',    'FK4pywqoa7g8xmv2yv4816wonrs', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_sub_sector',  'FKaymbe6u70936bgq191pmloqbw', 'tour_id',     'tour',          'tour_id');
CALL set_fk_cascade('tour_sub_sector',  'FKgrdfl5of1n792sm4kjer09to9', 'sub_sector_id','sub_sector',   'sub_sector_id');
CALL set_fk_cascade('tourcost',         'FK839gwatfjg6go96pe06nya2fw', 'tour_id',     'tour',          'tour_id');

-- Clean up the helper procedures.
DROP PROCEDURE IF EXISTS ensure_drop_fk;
DROP PROCEDURE IF EXISTS ensure_drop_column;
DROP PROCEDURE IF EXISTS ensure_add_column;
DROP PROCEDURE IF EXISTS set_fk_cascade;
