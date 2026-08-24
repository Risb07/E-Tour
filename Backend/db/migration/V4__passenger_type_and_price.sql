-- ============================================================================
-- V4: Passenger occupancy & child pricing (BRD 3.7).
--
-- Every passenger is now booked under an explicit type (adult / child /
-- infant) and an occupancy category, and the price that category produced is
-- stamped on the row so a booking can be audited or reprinted later without
-- re-deriving it from a TourCost sheet that may since have changed.
--
--   passenger_type   ADULT | CHILD | INFANT
--   passenger_price  category rate + any room supplement, per passenger
--
-- The existing `occupancy` column gains two new legal values,
-- CHILD_WITH_BED and CHILD_WITHOUT_BED. It is a plain VARCHAR holding the
-- enum name (Hibernate EnumType.STRING), so no DDL change is needed for
-- those - only the widening below, in case an older schema created it
-- narrower than the longest value ('CHILD_WITHOUT_BED', 17 chars).
--
-- Existing rows are left with NULL passenger_type / passenger_price: those
-- bookings were priced before this change and must not be retroactively
-- restated. Reads fall back to deriving the band from dob, exactly as the
-- application did before.
--
-- Idempotent: guarded by information_schema checks so it can safely be re-run.
--
-- Run manually (backup first if this is a real environment):
--   mysql -u root -p etour < db/migration/V4__passenger_type_and_price.sql
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

CALL ensure_add_column('passenger', 'passenger_type',  'varchar(20) NULL');
CALL ensure_add_column('passenger', 'passenger_price', 'decimal(12,2) NULL');

DROP PROCEDURE IF EXISTS ensure_add_column;

-- Widen `occupancy` if an earlier schema sized it below the longest enum name.
-- MODIFY is a no-op when the column is already varchar(20) or wider.
SET @occ_len = (SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'passenger'
                  AND COLUMN_NAME = 'occupancy');
SET @s = IF(@occ_len IS NOT NULL AND @occ_len < 20,
            'ALTER TABLE `passenger` MODIFY COLUMN `occupancy` varchar(20) NULL',
            'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @rc_len = (SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'room_charge'
                 AND COLUMN_NAME = 'occupancy');
SET @s = IF(@rc_len IS NOT NULL AND @rc_len < 20,
            'ALTER TABLE `room_charge` MODIFY COLUMN `occupancy` varchar(20) NOT NULL',
            'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
