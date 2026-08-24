-- ============================================================================
-- V6: Contact enquiries submitted from the public Contact page.
--
-- Standalone by design - no foreign key to users or customer. Anyone can
-- write in without an account, so the sender's details are captured as typed
-- rather than resolved against a profile.
--
-- status drives the admin inbox: NEW on arrival, then IN_PROGRESS/RESOLVED.
-- Indexed alongside created_at because the inbox is always read newest-first
-- and almost always filtered by state.
--
-- Idempotent: safe to re-run.
--
-- Run manually (backup first if this is a real environment):
--   mysql -u root -p etour < db/migration/V6__contact_enquiry.sql
-- ============================================================================

USE etour;

CREATE TABLE IF NOT EXISTS contact_enquiry (
  enquiry_id  BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(150) NOT NULL,
  email       VARCHAR(190) NOT NULL,
  phone       VARCHAR(20)  NULL,
  subject     VARCHAR(200) NOT NULL,
  message     VARCHAR(4000) NOT NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'NEW',
  created_at  DATETIME     NOT NULL,
  updated_at  DATETIME     NULL,
  PRIMARY KEY (enquiry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Guarded so re-running doesn't fail on an existing index.
SET @exists = (SELECT COUNT(1) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'contact_enquiry'
                 AND INDEX_NAME = 'idx_contact_enquiry_status_created');
SET @s = IF(@exists = 0,
            'CREATE INDEX idx_contact_enquiry_status_created ON contact_enquiry (status, created_at)',
            'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
