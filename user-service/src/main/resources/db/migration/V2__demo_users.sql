-- Demo accounts for local testing. Password for all three: Password123!
-- (Remove this migration, or gate it behind a profile, before a real launch.)
INSERT INTO users (id, email, password_hash, full_name, phone, role) VALUES
 ('11111111-1111-1111-1111-111111111111', 'customer@crafthinge.dev', '$2a$10$bEQBUTgFCcEwxTTbTBAOyue/a.xVK.PzKE1xQ4WHkoVZuYSU7Xo/i', 'Casey Customer', '+1-555-0101', 'CUSTOMER'),
 ('22222222-2222-2222-2222-222222222222', 'owner@crafthinge.dev',    '$2a$10$bEQBUTgFCcEwxTTbTBAOyue/a.xVK.PzKE1xQ4WHkoVZuYSU7Xo/i', 'Olivia Owner',   '+1-555-0102', 'RESTAURANT_OWNER'),
 ('33333333-3333-3333-3333-333333333333', 'driver@crafthinge.dev',   '$2a$10$bEQBUTgFCcEwxTTbTBAOyue/a.xVK.PzKE1xQ4WHkoVZuYSU7Xo/i', 'Dev Driver',     '+1-555-0103', 'DELIVERY_PARTNER');
