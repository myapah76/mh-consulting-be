ALTER TABLE contact_settings
    ADD COLUMN IF NOT EXISTS primary_phone_label VARCHAR(100),
    ADD COLUMN IF NOT EXISTS secondary_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS secondary_phone_label VARCHAR(100),
    ADD COLUMN IF NOT EXISTS working_hours VARCHAR(255),
    ADD COLUMN IF NOT EXISTS youtube_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE contact_settings
SET primary_phone = COALESCE(
        NULLIF(BTRIM(REGEXP_REPLACE(hotline1, '\s*\([^)]*\)\s*$', '')), ''),
        primary_phone
    ),
    primary_phone_label = COALESCE(
        primary_phone_label,
        NULLIF(SUBSTRING(hotline1 FROM '\(([^)]*)\)'), '')
    ),
    secondary_phone = COALESCE(
        secondary_phone,
        NULLIF(BTRIM(REGEXP_REPLACE(hotline2, '\s*\([^)]*\)\s*$', '')), '')
    ),
    secondary_phone_label = COALESCE(
        secondary_phone_label,
        NULLIF(SUBSTRING(hotline2 FROM '\(([^)]*)\)'), '')
    )
WHERE hotline1 IS NOT NULL
   OR hotline2 IS NOT NULL;

UPDATE contact_settings
SET address = COALESCE(
        address,
        '133/15 Đ. Ngô Đức Kế, Phường 12, Quận Bình Thạnh, TP. Hồ Chí Minh'
    ),
    primary_phone = COALESCE(primary_phone, '0903.024.116'),
    email = COALESCE(email, 'info@mhconsulting.vn'),
    working_hours = COALESCE(
        working_hours,
        'Thứ 2 - Thứ 7: 08:00 - 17:30'
    );

INSERT INTO contact_settings (
    singleton_key,
    address,
    primary_phone,
    primary_phone_label,
    secondary_phone,
    secondary_phone_label,
    email,
    working_hours,
    facebook_url,
    zalo_url,
    youtube_url,
    created_at,
    updated_at
)
SELECT
    TRUE,
    '133/15 Đ. Ngô Đức Kế, Phường 12, Quận Bình Thạnh, TP. Hồ Chí Minh',
    '0903.024.116',
    'Ms. Thảo',
    '0938.835.633',
    'Mr. Trí',
    'info@mhconsulting.vn',
    'Thứ 2 - Thứ 7: 08:00 - 17:30',
    NULL,
    'https://zalo.me/0903024116',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM contact_settings);

ALTER TABLE contact_settings
    ALTER COLUMN address SET NOT NULL,
    ALTER COLUMN primary_phone SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN working_hours SET NOT NULL;

ALTER TABLE contact_settings
    DROP COLUMN hotline1,
    DROP COLUMN hotline2,
    DROP COLUMN hotline3;
