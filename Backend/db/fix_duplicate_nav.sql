-- ===========================================================================
-- Removes duplicate top-level navigation entries from nav_menu_item.
--
-- WHY THIS HAPPENED: the header showed repeated tabs because two top-level
-- rows pointed at the same page with different labels:
--
--   /tours  : "Destinations" (has the sector dropdown) + "Explore tours"
--   /search : "Search tours" + "Explore Tours"
--
-- The old seed inserted 'Explore Tours' -> '/search' and 'Destinations' ->
-- '/search', so re-running it (or running an older seed version) produced a
-- header with two tabs for the same page. seed_content.sql now matches the
-- canonical rows below, so re-running it can't recreate the duplicates.
--
--   mysql -u root -p etour < fix_duplicate_nav.sql
-- ===========================================================================

-- 1. See what the header currently looks like (top-level rows only).
SELECT nav_menu_item_id, label, link
FROM nav_menu_item
WHERE parent_item_id IS NULL
ORDER BY sort_order, nav_menu_item_id;

-- 2. Keep the LOWEST-id row per (parent, link) at the top level and delete
--    the rest. Children are already attached to the surviving parent; if you
--    ever re-ran an old version of this script on a different dataset, run
--    the parent re-point UPDATE from the previous revision first (children
--    point at 'Destinations', which is always the lowest id here).
DELETE dup FROM nav_menu_item dup
JOIN nav_menu_item keeper
  ON keeper.parent_item_id <=> dup.parent_item_id
 AND keeper.link = dup.link
 AND keeper.nav_menu_item_id < dup.nav_menu_item_id
WHERE dup.parent_item_id IS NULL;

-- 3. Prevent it recurring. A menu should never contain two identical
--    (label, link) rows - this is also what makes seed_content.sql's
--    INSERT IGNORE effective. (Drop first in case you run this file twice.)
ALTER TABLE nav_menu_item DROP INDEX uk_nav_label_link;
ALTER TABLE nav_menu_item ADD CONSTRAINT uk_nav_label_link UNIQUE (label, link);

-- 4. Confirm - the header rows should show exactly one tab per link and the
--    second query should return no rows.
SELECT nav_menu_item_id, label, link
FROM nav_menu_item
WHERE parent_item_id IS NULL
ORDER BY sort_order, nav_menu_item_id;

SELECT link, COUNT(*) AS copies
FROM nav_menu_item
WHERE parent_item_id IS NULL
GROUP BY link
HAVING COUNT(*) > 1;
