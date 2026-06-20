INSERT INTO products (code, name, category, safety_stock)
VALUES
    ('PRD-001', 'コピー用紙 A4', '事務用品', 20),
    ('PRD-002', 'ボールペン 黒', '事務用品', 50),
    ('PRD-003', '梱包テープ', '梱包資材', 15)
ON CONFLICT (code) DO NOTHING;

INSERT INTO stock_movements (product_id, movement_type, quantity, note)
SELECT id, 'IN', 100, '初期在庫'
FROM products
WHERE code = 'PRD-001'
  AND NOT EXISTS (
      SELECT 1 FROM stock_movements sm
      WHERE sm.product_id = products.id AND sm.note = '初期在庫'
  );

INSERT INTO stock_movements (product_id, movement_type, quantity, note)
SELECT id, 'IN', 200, '初期在庫'
FROM products
WHERE code = 'PRD-002'
  AND NOT EXISTS (
      SELECT 1 FROM stock_movements sm
      WHERE sm.product_id = products.id AND sm.note = '初期在庫'
  );

INSERT INTO stock_movements (product_id, movement_type, quantity, note)
SELECT id, 'IN', 40, '初期在庫'
FROM products
WHERE code = 'PRD-003'
  AND NOT EXISTS (
      SELECT 1 FROM stock_movements sm
      WHERE sm.product_id = products.id AND sm.note = '初期在庫'
  );

INSERT INTO stock_movements (product_id, movement_type, quantity, note)
SELECT id, 'OUT', 30, 'サンプル出庫'
FROM products
WHERE code = 'PRD-003'
  AND NOT EXISTS (
      SELECT 1 FROM stock_movements sm
      WHERE sm.product_id = products.id AND sm.note = 'サンプル出庫'
  );
INSERT INTO app_users (username, password, role, enabled)
VALUES (
    'admin',
    '$2a$10$TlvgzQFP3Y6NGea48CmuSOth0lOaiU6lh2LADpuRGGQV6YG1Ka0Ea',
    'ADMIN',
    TRUE
)
ON CONFLICT (username) DO NOTHING;
