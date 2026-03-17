-- 1. On vide la table et on remet les compteurs d'ID à 1
TRUNCATE TABLE products RESTART IDENTITY CASCADE;

-- 2. On insère tes données de test
INSERT INTO products (label, price, description) VALUES ('Clavier Mécanique', 89.99, 'Switch Red');
INSERT INTO products (label, price, description) VALUES ('Souris Gamer', 45.50, '16000 DPI');
INSERT INTO products (label, price, description) VALUES ('Screem Tactic', 235.50, '16 Pouce');
