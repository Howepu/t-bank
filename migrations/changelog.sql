CREATE TABLE chat (
                      chat_id BIGINT NOT NULL UNIQUE PRIMARY KEY
);

CREATE TABLE link (
                      id         BIGSERIAL PRIMARY KEY,
                      url        TEXT NOT NULL,
                      filters    TEXT[] DEFAULT '{}',
                      created_at TIMESTAMP DEFAULT NULL,
                      chat_id    BIGINT,
                      CONSTRAINT fk_link_chat FOREIGN KEY (chat_id) REFERENCES chat (chat_id) ON DELETE CASCADE
);

CREATE TABLE tag (
                     id BIGSERIAL PRIMARY KEY,
                     name TEXT UNIQUE NOT NULL
);

CREATE TABLE link_tag (
                          link_id BIGINT NOT NULL,
                          tag_id BIGINT NOT NULL,
                          PRIMARY KEY (link_id, tag_id),
                          FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE,
                          FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);
