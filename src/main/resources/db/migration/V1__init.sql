CREATE TABLE catalog_users (
    id                VARCHAR(255) PRIMARY KEY,
    catalog_id        VARCHAR(255) NOT NULL,
    name              VARCHAR(500) NOT NULL,
    email             VARCHAR(500),
    telephone_number  VARCHAR(100)
);

CREATE INDEX idx_catalog_users_catalog_id ON catalog_users (catalog_id);

CREATE TABLE editable_fields (
    catalog_id          VARCHAR(255) PRIMARY KEY,
    domain_code_list_id VARCHAR(255)
);

CREATE TABLE internal_fields (
    id              VARCHAR(255) PRIMARY KEY,
    catalog_id      VARCHAR(255) NOT NULL,
    label           JSONB NOT NULL,
    description     JSONB NOT NULL,
    type            VARCHAR(50) NOT NULL,
    location        VARCHAR(50) NOT NULL,
    code_list_id    VARCHAR(255),
    enable_filter   BOOLEAN
);

CREATE INDEX idx_internal_fields_catalog_id ON internal_fields (catalog_id);
CREATE INDEX idx_internal_fields_catalog_type_codelist ON internal_fields (catalog_id, type, code_list_id);

CREATE TABLE catalog_designs (
    catalog_id        VARCHAR(255) PRIMARY KEY,
    background_color  VARCHAR(50),
    font_color        VARCHAR(50),
    logo_description  VARCHAR(1000),
    has_logo          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE catalog_logos (
    catalog_id    VARCHAR(255) PRIMARY KEY,
    content_type  VARCHAR(100) NOT NULL,
    base64_logo   TEXT NOT NULL,
    filename      VARCHAR(500) NOT NULL
);

CREATE TABLE code_lists (
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(500) NOT NULL,
    catalog_id  VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    codes       JSONB NOT NULL
);

CREATE INDEX idx_code_lists_catalog_id ON code_lists (catalog_id);
