-- V3__add_color_to_martial_art.sql

ALTER TABLE martial_art ADD COLUMN color VARCHAR(20) DEFAULT 'brown';

UPDATE martial_art SET color = 'brown'   WHERE code = 'WING_TSUN';
UPDATE martial_art SET color = '#C6A800' WHERE code = 'WING_TSUN_KIDS';
UPDATE martial_art SET color = '#1565C0' WHERE code = 'CHI_KUNG';
UPDATE martial_art SET color = '#2E7D32' WHERE code = 'ESCRIMA';
UPDATE martial_art SET color = '#6A1B9A' WHERE code = 'CHANBARA';