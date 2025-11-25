-- Sample data for testing the e-commerce layered architecture

-- Insert sample users
INSERT INTO users (id, username, email, password, full_name, phone_number, created_at, updated_at)
VALUES
    (1, 'john_doe', 'john@example.com', 'password123', 'John Doe', '+1-555-0101', NOW(), NOW()),
    (2, 'jane_smith', 'jane@example.com', 'password123', 'Jane Smith', '+1-555-0102', NOW(), NOW()),
    (3, 'bob_wilson', 'bob@example.com', 'password123', 'Bob Wilson', '+1-555-0103', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert sample products
INSERT INTO products (id, name, description, price, stock_quantity, sku, created_at, updated_at)
VALUES
    (1, 'Laptop', 'High-performance laptop for professionals', 1299.99, 50, 'LAPTOP-001', NOW(), NOW()),
    (2, 'Wireless Mouse', 'Ergonomic wireless mouse', 29.99, 200, 'MOUSE-001', NOW(), NOW()),
    (3, 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 149.99, 100, 'KEYBOARD-001', NOW(), NOW()),
    (4, 'USB-C Hub', '7-in-1 USB-C hub with HDMI and ethernet', 49.99, 150, 'HUB-001', NOW(), NOW()),
    (5, 'Laptop Stand', 'Adjustable aluminum laptop stand', 39.99, 75, 'STAND-001', NOW(), NOW()),
    (6, 'Webcam', '1080p HD webcam with microphone', 79.99, 120, 'WEBCAM-001', NOW(), NOW()),
    (7, 'Headphones', 'Noise-cancelling wireless headphones', 249.99, 80, 'HEADPHONES-001', NOW(), NOW()),
    (8, 'External SSD', '1TB portable SSD', 129.99, 90, 'SSD-001', NOW(), NOW()),
    (9, 'Monitor', '27-inch 4K monitor', 499.99, 40, 'MONITOR-001', NOW(), NOW()),
    (10, 'Desk Lamp', 'LED desk lamp with USB charging', 34.99, 110, 'LAMP-001', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequences
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
