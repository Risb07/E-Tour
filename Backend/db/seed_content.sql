-- ===========================================================================
-- eTour — site content seed
--
-- The frontend no longer hard-codes ANY copy or imagery. It reads these rows
-- from the `content` table. Run this once, then edit the values in the DB (or
-- through the admin Content screen) to change what the site says.
--
-- Anything you don't insert simply doesn't render — sections hide themselves
-- rather than showing placeholder text.
--
-- CONVENTION for repeating blocks (why-choose-us cards, FAQ entries):
--   content_value holds "Heading :: Body text"
--   A value with no "::" is treated as body-only.
--
--   mysql -u root -p etour < seed_content.sql
-- ===========================================================================

-- --- Global -----------------------------------------------------------------
INSERT INTO content (content_key, page_name, language_code, content_value, display_order, status) VALUES
('site.brand', 'global', 'en', 'eTour', 0, 1);

-- --- Home hero (media_url is the background image) --------------------------
INSERT INTO content (content_key, page_name, language_code, content_value, media_url, display_order, status) VALUES
('home.hero', 'home', 'en', 'Home hero background', '/uploads/hero/hero.jpg', 0, 1);

INSERT INTO content (content_key, page_name, language_code, content_value, display_order, status) VALUES
('home.hero.eyebrow',      'home', 'en', 'Plan less, wander more', 1, 1),
('home.hero.title',        'home', 'en', 'Every great journey starts with one honest plan.', 2, 1),
('home.hero.subtitle',     'home', 'en', 'Browse curated tours, compare transparent pricing, and book with confidence.', 3, 1),
('home.search.placeholder','home', 'en', 'Where do you want to go?', 4, 1);

-- --- Home: why choose us (keys must start with "home.why.") -----------------
INSERT INTO content (content_key, page_name, language_code, content_value, display_order, status) VALUES
('home.why.eyebrow', 'home', 'en', 'Why eTour', 0, 1),
('home.why.title',   'home', 'en', 'Built for travellers who plan ahead', 1, 1),
('home.why.01', 'home', 'en', 'Verified tours :: Every itinerary is reviewed before it goes live.', 2, 1),
('home.why.02', 'home', 'en', 'Transparent pricing :: See the full cost breakdown before you book, always.', 3, 1),
('home.why.03', 'home', 'en', 'Flexible booking :: Cancel or adjust your plans from your dashboard.', 4, 1),
('home.why.04', 'home', 'en', 'Real support :: Reach a real person when your trip needs attention.', 5, 1);

-- --- Home: FAQ (keys must start with "home.faq.") ---------------------------
INSERT INTO content (content_key, page_name, language_code, content_value, display_order, status) VALUES
('home.faq.eyebrow', 'home', 'en', 'Good to know', 0, 1),
('home.faq.title',   'home', 'en', 'Frequently asked questions', 1, 1),
('home.faq.01', 'home', 'en', 'How do I book a tour? :: Open any tour, choose a departure date, add passenger details, and confirm.', 2, 1),
('home.faq.02', 'home', 'en', 'Can I cancel a booking? :: Yes, from My Bookings in your dashboard, as long as it is not already completed.', 3, 1),
('home.faq.03', 'home', 'en', 'How do add-ons work? :: Optional extras are added during booking and their price is locked in at that time.', 4, 1);

-- --- Footer -----------------------------------------------------------------
INSERT INTO content (content_key, page_name, language_code, content_value, display_order, status) VALUES
('footer.tagline',              'footer', 'en', 'Curated tours, transparent pricing, and a booking experience built for travellers who plan ahead.', 0, 1),
('footer.links.heading',        'footer', 'en', 'Explore', 1, 1),
('footer.contact.heading',      'footer', 'en', 'Contact', 2, 1),
('footer.contact.phone',        'footer', 'en', '+91 22 1234 5678', 3, 1),
('footer.contact.email',        'footer', 'en', 'hello@tourindia.example', 4, 1),
('footer.contact.address',      'footer', 'en', 'H.O: 111, L J Road, Dadar, Mumbai 400028', 5, 1),
('footer.newsletter.heading',   'footer', 'en', 'Stay in the loop', 6, 1),
('footer.newsletter.blurb',     'footer', 'en', 'Get new tours and offers in your inbox.', 7, 1),
('footer.newsletter.placeholder','footer','en', 'you@example.com', 8, 1),
('footer.newsletter.cta',       'footer', 'en', 'Subscribe', 9, 1),
('footer.copyright',            'footer', 'en', '© 2026 TourIndia Travels Pvt Ltd. All rights reserved.', 10, 1);

-- --- Footer policy links (keys must start with "footer.policy.") ------------
-- link_url is optional; without it the label renders as plain text.
INSERT INTO content (content_key, page_name, language_code, content_value, link_url, display_order, status) VALUES
('footer.policy.01', 'footer', 'en', 'Contact Us', '/contact', 0, 1),
('footer.policy.02', 'footer', 'en', 'Site map',   '/sitemap', 1, 1),
('footer.policy.03', 'footer', 'en', 'Careers',    '/careers', 2, 1);

-- --- Top navigation (nav_menu_item table, not content) ----------------------
-- The navbar and the footer "Explore" list both read this table.
--
-- INSERT IGNORE + the unique (label, link) index from fix_duplicate_nav.sql
-- makes re-running this file safe. Without that guard, running the seed twice
-- produced a duplicated "Explore Tours" tab in the header.
--
-- Labels/links here MUST match the rows the site already has (or what a fresh
-- seed creates). In particular "Destinations" points at /tours (the catalog
-- with the sector dropdown) and the search tab is labelled "Search tours" -
-- a second top-level row pointing at /search is what made "Explore Tours"
-- appear twice in the header.
INSERT IGNORE INTO nav_menu_item (label, link, sort_order, active) VALUES
('Home',          '/home',   1, 1),
('Destinations',  '/tours',  2, 1),
('Search tours',  '/search', 3, 1);

-- Children of "Destinations" - these are what make the dropdown appear.
-- A parent with no children renders as a plain link, by design.
-- Point each child at a real sector id from your `sector` table.
INSERT IGNORE INTO nav_menu_item (label, link, sort_order, active, parent_item_id)
SELECT s.name, CONCAT('/sectors/', s.sector_id), s.sort_order, 1, p.nav_menu_item_id
FROM sector s
CROSS JOIN (
    SELECT nav_menu_item_id FROM nav_menu_item WHERE label = 'Destinations' LIMIT 1
) p
WHERE s.active = 1;

-- --- Home page crawling ticker (crawling_text table) ------------------------
INSERT INTO crawling_text (text, sort_order, is_active) VALUES
('Monsoon offers now live — up to 20% off selected domestic tours.', 1, 1),
('New: Leh-Ladakh departures added for the summer season.', 2, 1);
