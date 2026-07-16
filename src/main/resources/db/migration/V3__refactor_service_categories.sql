CREATE TABLE service_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_service_category_slug UNIQUE (slug)
);

INSERT INTO service_categories (slug, name, active, display_order) VALUES
('thanh-lap', 'Thành lập doanh nghiệp', TRUE, 0),
('ke-toan', 'Kế toán', TRUE, 1),
('thue', 'Thuế', TRUE, 2),
('khac', 'Dịch vụ khác', TRUE, 3);

ALTER TABLE business_services
    ADD COLUMN category_id UUID;

UPDATE business_services service
SET category_id = category.id
FROM service_categories category
WHERE category.slug = CASE service.category
    WHEN 'THANH_LAP' THEN 'thanh-lap'
    WHEN 'KE_TOAN' THEN 'ke-toan'
    WHEN 'THUE' THEN 'thue'
    WHEN 'KHAC' THEN 'khac'
END;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM business_services WHERE category_id IS NULL) THEN
        RAISE EXCEPTION 'Cannot migrate business service with an unsupported category';
    END IF;
END $$;

ALTER TABLE business_services
    ALTER COLUMN category_id SET NOT NULL;

ALTER TABLE business_services
    ADD CONSTRAINT fk_business_service_category
        FOREIGN KEY (category_id)
        REFERENCES service_categories(id);

CREATE INDEX idx_service_categories_active_order
    ON service_categories(active, display_order, name);

CREATE INDEX idx_business_services_category_id
    ON business_services(category_id);

DROP INDEX idx_business_services_category;

ALTER TABLE business_services
    DROP COLUMN category;
