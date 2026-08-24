-- ===========================================================================
-- Starter multipath rules.
--
-- A "multipath" tour is ONE tour row reachable by several navigation routes.
-- These rules create the links automatically instead of you ticking
-- categories on every tour by hand.
--
-- HOW TO USE
--   1. Run this file (it creates the categories it needs, then the rules).
--   2. Admin -> Multipath Rules -> Preview   (shows what WOULD change)
--   3. Admin -> Multipath Rules -> Apply     (writes the links)
--
-- Re-running Apply later is safe and is how new tours get picked up.
--
--   mysql -u root -p etour < seed_multipath_rules.sql
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 1. Cross-cutting categories the rules target.
--    These are "tags" rather than places - a tour can be Domestic AND a
--    Weekend Getaway AND a Budget trip, which is exactly what makes it
--    multipath.
-- ---------------------------------------------------------------------------
INSERT INTO category (category_name, description, category_code, is_featured, status)
SELECT * FROM (SELECT 'Weekend Getaways' AS n, 'Short trips of 3 days or fewer' AS d, 'DOM' AS c, 'Y' AS f, 1 AS s) AS t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Weekend Getaways');

INSERT INTO category (category_name, description, category_code, is_featured, status)
SELECT * FROM (SELECT 'Extended Journeys', 'Trips of 8 days or more', 'INT', 'N', 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Extended Journeys');

INSERT INTO category (category_name, description, category_code, is_featured, status)
SELECT * FROM (SELECT 'Budget Friendly', 'Great value trips under 25,000', 'DOM', 'Y', 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Budget Friendly');

INSERT INTO category (category_name, description, category_code, is_featured, status)
SELECT * FROM (SELECT 'Luxury Collection', 'Premium trips above 75,000', 'INT', 'Y', 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Luxury Collection');

INSERT INTO category (category_name, description, category_code, is_featured, status)
SELECT * FROM (SELECT 'All Tours', 'Every published tour', 'DOM', 'N', 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'All Tours');

-- ---------------------------------------------------------------------------
-- 2. The rules.
--
--    match_field  + match_operator + match_value  ->  target_category
--
--    target_sub_sector_id is left NULL here: that option also generates a
--    marketing product record (name, image, price) which you would then have
--    to curate. Set it per-rule in the admin screen when you actually want
--    the Sectors -> Sub-Sector -> Product path for those tours.
-- ---------------------------------------------------------------------------

-- Duration-based
INSERT INTO tour_tag_rule (name, match_field, match_operator, match_value, target_category_id, priority, active)
SELECT 'Short trips -> Weekend Getaways', 'DURATION_DAYS', 'LESS_THAN', '4',
       (SELECT category_id FROM category WHERE category_name = 'Weekend Getaways'), 10, 1
WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'Short trips -> Weekend Getaways');

INSERT INTO tour_tag_rule (name, match_field, match_operator, match_value, target_category_id, priority, active)
SELECT 'Long trips -> Extended Journeys', 'DURATION_DAYS', 'GREATER_THAN', '7',
       (SELECT category_id FROM category WHERE category_name = 'Extended Journeys'), 20, 1
WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'Long trips -> Extended Journeys');

-- Price-based
INSERT INTO tour_tag_rule (name, match_field, match_operator, match_value, target_category_id, priority, active)
SELECT 'Under 25k -> Budget Friendly', 'BASE_PRICE', 'LESS_THAN', '25000',
       (SELECT category_id FROM category WHERE category_name = 'Budget Friendly'), 30, 1
WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'Under 25k -> Budget Friendly');

INSERT INTO tour_tag_rule (name, match_field, match_operator, match_value, target_category_id, priority, active)
SELECT 'Above 75k -> Luxury Collection', 'BASE_PRICE', 'GREATER_THAN', '75000',
       (SELECT category_id FROM category WHERE category_name = 'Luxury Collection'), 40, 1
WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'Above 75k -> Luxury Collection');

-- Catch-all: every active tour also lives under "All Tours"
INSERT INTO tour_tag_rule (name, match_field, match_operator, target_category_id, priority, active)
SELECT 'Everything -> All Tours', 'ALL', 'ANY',
       (SELECT category_id FROM category WHERE category_name = 'All Tours'), 90, 1
WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'Everything -> All Tours');

-- Code-based: only meaningful if you have matching categories already.
-- Uncomment and adjust the category name to suit your data.
-- INSERT INTO tour_tag_rule (name, match_field, match_operator, match_value, target_category_id, priority, active)
-- SELECT 'INT code -> International', 'TOUR_CODE', 'EQUALS', 'INT',
--        (SELECT category_id FROM category WHERE category_name = 'International'), 5, 1
-- WHERE NOT EXISTS (SELECT 1 FROM tour_tag_rule WHERE name = 'INT code -> International');

-- ---------------------------------------------------------------------------
-- 3. Check what you created.
-- ---------------------------------------------------------------------------
SELECT r.rule_id, r.name, r.match_field, r.match_operator, r.match_value,
       c.category_name AS target_category, r.active
FROM tour_tag_rule r
JOIN category c ON c.category_id = r.target_category_id
ORDER BY r.priority;

-- ---------------------------------------------------------------------------
-- 4. After running Apply in the admin screen, this shows how many paths each
--    tour now has. Anything with 2+ is a multipath tour.
-- ---------------------------------------------------------------------------
-- SELECT t.tour_id, t.title, COUNT(tc.category_id) AS paths
-- FROM tour t
-- LEFT JOIN tour_category tc ON tc.tour_id = t.tour_id
-- WHERE t.status = 'ACTIVE'
-- GROUP BY t.tour_id, t.title
-- ORDER BY paths DESC;
