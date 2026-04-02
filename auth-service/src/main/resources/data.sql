-- Seed roles table on startup (only inserts if not already present)
INSERT INTO roles (name) SELECT 'ROLE_CUSTOMER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER');
INSERT INTO roles (name) SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');
