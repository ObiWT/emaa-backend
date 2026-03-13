-- =========================
-- V2: Platby per bojové umenie
-- =========================

-- Pridanie platobných stĺpcov do martial_art
ALTER TABLE martial_art ADD COLUMN credit_payment INT;
ALTER TABLE martial_art ADD COLUMN monthly_payment INT;
ALTER TABLE martial_art ADD COLUMN yearly_payment INT;

-- Odstránenie platobných stĺpcov zo school
ALTER TABLE school DROP COLUMN credit_payment;
ALTER TABLE school DROP COLUMN monthly_payment;
ALTER TABLE school DROP COLUMN yearly_payment;