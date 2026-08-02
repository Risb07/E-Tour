CREATE INDEX idx_users_status
ON users(status);

CREATE INDEX idx_tour_base_price
ON tour(base_price);

CREATE INDEX idx_tour_duration
ON tour(duration_days);

CREATE INDEX idx_category_status
ON category(status);

CREATE INDEX idx_customer_name
ON customer(full_name);

CREATE INDEX idx_schedule_departure
ON tour_schedule(departure_date);

CREATE INDEX idx_schedule_return
ON tour_schedule(return_date);

CREATE INDEX idx_booking_customer_status
ON booking(customer_id, booking_status);

CREATE INDEX idx_review_rating
ON review(rating);

CREATE INDEX idx_tour_cost_status
ON TOURCOST(status);

CREATE INDEX idx_itinerary_tour_day
ON itinerary(tour_id, day_number);

CREATE INDEX idx_cart_user_tour
ON cart(user_id, tour_id);

CREATE INDEX idx_passenger_name
ON passenger(full_name);