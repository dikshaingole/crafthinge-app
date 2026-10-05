-- Runs once when the local Postgres container is first created.
-- Same layout as Azure: one server, one database per service.
CREATE DATABASE users;
CREATE DATABASE restaurants;
CREATE DATABASE menu;
CREATE DATABASE carts;
CREATE DATABASE orders;
CREATE DATABASE payments;
CREATE DATABASE deliveries;
