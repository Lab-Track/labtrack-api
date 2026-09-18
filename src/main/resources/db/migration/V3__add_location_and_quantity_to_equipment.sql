ALTER TABLE equipment
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0);

ALTER TABLE equipment
    ALTER COLUMN quantity DROP DEFAULT;
