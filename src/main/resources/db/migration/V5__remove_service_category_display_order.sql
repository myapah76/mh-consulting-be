DROP INDEX IF EXISTS idx_service_categories_active_order;

ALTER TABLE service_categories
    DROP COLUMN IF EXISTS display_order;
