-- ===========================================================================
-- Publishes DRAFT tours so they appear in category listings and search.
--
-- WHY YOUR CATEGORY PAGES ARE EMPTY:
-- Tours default to status = 'DRAFT' (both the entity default and the admin
-- form). Search and category listings deliberately return only ACTIVE tours,
-- so a tour can be correctly assigned to a category and still be invisible.
--
-- Run step 1 first to see what you have, then step 2 to publish.
--
--   mysql -u root -p etour < publish_draft_tours.sql
-- ===========================================================================

-- 1. What state are your tours in?
SELECT status, COUNT(*) AS tours
FROM tour
GROUP BY status;

-- 1b. Which tours are assigned to a category but hidden from customers?
SELECT t.tour_id, t.title, t.status, c.category_name
FROM tour t
JOIN tour_category tc ON tc.tour_id = t.tour_id
JOIN category c       ON c.category_id = tc.category_id
WHERE t.status <> 'ACTIVE'
ORDER BY c.category_name, t.title;

-- 2. Publish every draft tour that is assigned to at least one category.
--    Scoped to categorised tours on purpose - a genuinely unfinished tour
--    with no category stays hidden.
UPDATE tour t
JOIN (SELECT DISTINCT tour_id FROM tour_category) tc ON tc.tour_id = t.tour_id
SET t.status = 'ACTIVE'
WHERE t.status = 'DRAFT';

-- 2b. If you want EVERY tour public instead, use this rather than step 2:
-- UPDATE tour SET status = 'ACTIVE' WHERE status = 'DRAFT';

-- 3. Confirm.
SELECT status, COUNT(*) AS tours
FROM tour
GROUP BY status;

-- 4. Sanity check: tours now visible per category.
SELECT c.category_name, COUNT(t.tour_id) AS active_tours
FROM category c
LEFT JOIN tour_category tc ON tc.category_id = c.category_id
LEFT JOIN tour t           ON t.tour_id = tc.tour_id AND t.status = 'ACTIVE'
GROUP BY c.category_id, c.category_name
ORDER BY c.category_name;
