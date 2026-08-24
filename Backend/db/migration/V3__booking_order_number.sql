-- ============================================================================
-- V3: Add `order_number` to booking (BRD 3.7 - unique order number generated
-- on successful payment).
--
-- Idempotent: guarded by information_schema check so it can safely be re-run.
--
-- Run manually (backup first if this is a real environment):
--   mysql -u root -p etour < db/migration/V3__booking_order_number.sql
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

CALL ensure_add_column('booking', 'order_number', 'varchar(40) NULL');

DROP PROCEDURE IF EXISTS ensure_add_column;
