CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200),
    role VARCHAR(30) NOT NULL CHECK (role = 'ADMIN'),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_admin_email UNIQUE (email)
);

CREATE TABLE business_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(200) NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('THANH_LAP','KE_TOAN','THUE','KHAC')),
    short_description VARCHAR(1000) NOT NULL,
    icon VARCHAR(100),
    full_content TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_business_service_slug UNIQUE (slug)
);

CREATE TABLE service_detail_points (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES business_services(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0)
);
CREATE TABLE service_benefits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES business_services(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0)
);
CREATE TABLE service_process_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES business_services(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0)
);

CREATE TABLE page_sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    page_key VARCHAR(100) NOT NULL,
    section_key VARCHAR(100) NOT NULL,
    title VARCHAR(300),
    subtitle VARCHAR(500),
    content TEXT,
    image_url VARCHAR(2000),
    button_label VARCHAR(200),
    button_url VARCHAR(2000),
    display_order INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_page_section UNIQUE (page_key, section_key)
);

CREATE TABLE contact_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    singleton_key BOOLEAN NOT NULL DEFAULT TRUE UNIQUE CHECK (singleton_key),
    company_name VARCHAR(300),
    tagline VARCHAR(500),
    primary_phone VARCHAR(50),
    hotline1 VARCHAR(100),
    hotline2 VARCHAR(100),
    hotline3 VARCHAR(100),
    email VARCHAR(320),
    address VARCHAR(1000),
    zalo_url VARCHAR(2000),
    facebook_url VARCHAR(2000),
    google_maps_url VARCHAR(2000),
    business_registration_text VARCHAR(2000),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consultation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name VARCHAR(200) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(320),
    service_id UUID REFERENCES business_services(id),
    service_title_snapshot VARCHAR(200),
    message TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW','CONTACTED','COMPLETED','CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_business_services_category ON business_services(category);
CREATE INDEX idx_business_services_active ON business_services(active);
CREATE INDEX idx_page_sections_page_key ON page_sections(page_key);
CREATE INDEX idx_consultation_status ON consultation_requests(status);
CREATE INDEX idx_consultation_created_at ON consultation_requests(created_at DESC);
CREATE INDEX idx_consultation_service_id ON consultation_requests(service_id);
CREATE INDEX idx_service_detail_order ON service_detail_points(service_id, display_order);
CREATE INDEX idx_service_benefit_order ON service_benefits(service_id, display_order);
CREATE INDEX idx_service_process_order ON service_process_steps(service_id, display_order);
