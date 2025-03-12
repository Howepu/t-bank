CREATE TABLE chat (
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE
);


CREATE TABLE link (
    id         BIGSERIAL PRIMARY KEY,
    url        TEXT NOT NULL UNIQUE,
    filters    TEXT[] DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NULL
);

CREATE TABLE chat_link (
    chat_id BIGINT NOT NULL REFERENCES chat(id) ON DELETE CASCADE,
    link_id BIGINT NOT NULL REFERENCES link(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id)
);


CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE link_tag (
    link_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (link_id, tag_id),
    FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);

