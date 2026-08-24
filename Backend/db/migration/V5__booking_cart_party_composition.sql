-- ============================================================================
-- V5: Party composition on booking and cart.
--
-- The tour page now asks for adults and children separately instead of a
-- single passenger count, so the mix that was sold is recorded rather than
-- re-derived from dates of birth afterwards:
--
--   adult_count  travellers sold as adults  (must be >= 1 on a new booking)
--   child_count  travellers sold as children
--
-- adult_count + child_count = number_of_passengers for anything created after
-- this change. Existing rows keep NULL for both: they were sold before the
-- split existed and must not be restated. Readers fall back to
-- number_of_passengers, and the at-least-one-adult rule is then enforced from
-- the passengers' dates of birth instead (see
-- BookingServiceImpl#validatePassengerComposition).
--
-- Deliberately NOT NULL-constrained for that reason - a NOT NULL with a
-- default would silently invent a composition for historical bookings.
--
-- Idempotent: guarded by information_schema checks so it can safely be re-run.
--
-- Run manually (backup first if this is a real environment):
--   mysql -u root -p etour < db/migration/V5__booking_cart_party_composition.sql
-- ============================================================================

USE etour;

DELIMITER $$
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
DELIMITER ;

CALL ensure_add_column('booking', 'adult_count', 'int NULL');
CALL ensure_add_column('booking', 'child_count', 'int NULL');

CALL ensure_add_column('cart', 'adult_count', 'int NULL');
CALL ensure_add_column('cart', 'child_count', 'int NULL');

DROP PROCEDURE IF EXISTS ensure_add_column;
