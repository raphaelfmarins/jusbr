CREATE TABLE short_url (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(7)    NOT NULL UNIQUE,
    original_url VARCHAR(2048) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    expires_at   TIMESTAMPTZ
);

CREATE INDEX idx_short_url_created_at ON short_url (created_at DESC);

CREATE TABLE click (
    id           BIGSERIAL PRIMARY KEY,
    short_url_id BIGINT       NOT NULL REFERENCES short_url (id),
    clicked_at   TIMESTAMPTZ  NOT NULL,
    referrer     VARCHAR(2048),
    user_agent   VARCHAR(1024),
    ip           VARCHAR(64)
);

CREATE INDEX idx_click_short_url_clicked_at ON click (short_url_id, clicked_at);
