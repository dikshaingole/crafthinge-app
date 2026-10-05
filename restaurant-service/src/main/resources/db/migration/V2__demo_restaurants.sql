-- All owned by the demo owner (owner@crafthinge.dev). IDs 1..8 are referenced by food-service seed data.
INSERT INTO restaurants (owner_id, name, cuisine, description, address, city, emoji, rating, delivery_time_minutes, delivery_fee, featured) VALUES
 ('22222222-2222-2222-2222-222222222222', 'Smokestack Burger Co.', 'American',      'Griddled smash burgers, crinkle fries and thick shakes.',        '412 Elm St',       'Austin',      '🍔', 4.7, 25, 1.99, TRUE),
 ('22222222-2222-2222-2222-222222222222', 'Casa Verde Taqueria',   'Mexican',       'Street tacos on house-pressed tortillas, salsas made daily.',   '88 Mission Ave',   'San Diego',   '🌮', 4.8, 20, 2.49, TRUE),
 ('22222222-2222-2222-2222-222222222222', 'Nonna''s Oven',          'Italian',       'Wood-fired pizza and fresh pasta from a family recipe book.',   '17 Mulberry St',   'New York',    '🍕', 4.6, 35, 2.99, TRUE),
 ('22222222-2222-2222-2222-222222222222', 'Saffron House',         'Indian',        'Slow-cooked curries, tandoor breads and biryani.',             '230 Devon Ave',    'Chicago',     '🍛', 4.7, 40, 2.99, FALSE),
 ('22222222-2222-2222-2222-222222222222', 'Tsuki Ramen Bar',       'Japanese',      'Twelve-hour tonkotsu broth and hand-rolled sushi.',            '501 Pike St',      'Seattle',     '🍜', 4.9, 30, 3.49, TRUE),
 ('22222222-2222-2222-2222-222222222222', 'Bangkok Street Kitchen','Thai',          'Wok-fired noodles and curries, spice level your call.',         '64 Sunset Blvd',   'Los Angeles', '🍲', 4.5, 30, 2.49, FALSE),
 ('22222222-2222-2222-2222-222222222222', 'Olive & Ember',         'Mediterranean', 'Charcoal-grilled kebabs, bowls and warm pita.',                '9 Harbor Rd',      'Boston',      '🥙', 4.6, 25, 1.99, FALSE),
 ('22222222-2222-2222-2222-222222222222', 'Sunny Side Diner',      'Breakfast',     'All-day pancakes, eggs any style and bottomless coffee.',      '300 Peachtree St', 'Atlanta',     '🥞', 4.4, 20, 0.99, FALSE);
