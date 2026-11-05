CREATE TABLE IF NOT EXISTS users
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),

    username       TEXT        NOT NULL,
    CONSTRAINT u_username_l CHECK (char_length(username) BETWEEN 2 AND 100),

    email          TEXT        NOT NULL, -- not unique because must be unique only for not deactivated accounts
    CONSTRAINT u_email_l CHECK (char_length(email) <= 254),

    password_hash  TEXT        NOT NULL,
    CONSTRAINT u_password_l CHECK (char_length(password_hash) = 60),

    avatar_img     UUID,                 -- file name

    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    deactivated_at TIMESTAMPTZ,
    last_login_at  TIMESTAMPTZ,
    last_seen_at   TIMESTAMPTZ
);
-- Optional unique index, email unique only when account is not deactivated
CREATE UNIQUE INDEX IF NOT EXISTS unique_email_when_not_deactivated
    ON users (lower(email))
    WHERE deactivated_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS unique_username_when_not_deactivated
    ON users (lower(username))
    WHERE deactivated_at IS NULL;

CREATE TABLE IF NOT EXISTS sessions
(
    id                 UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id            UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    refresh_token_hash TEXT        NOT NULL UNIQUE,
    CONSTRAINT s_refresh_token_hash_l CHECK (char_length(refresh_token_hash) = 64),

    device_info        TEXT        NOT NULL,

    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at         TIMESTAMPTZ NOT NULL,
    revoked_at         TIMESTAMPTZ,

    CONSTRAINT s_expires_at_rule CHECK ( expires_at >= created_at )
);
CREATE INDEX IF NOT EXISTS index_sessions_user_id ON sessions (user_id);


CREATE TABLE IF NOT EXISTS applications
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    name           TEXT        NOT NULL,
    played_seconds BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT a_played_seconds_rule CHECK (played_seconds >= 0),

    background_img UUID,
    logo_img       UUID,
    icon_img       UUID,
    cover_img      UUID,

    is_private     BOOLEAN     NOT NULL DEFAULT FALSE,

    added_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS index_applications_user_id ON applications (user_id);


CREATE TABLE IF NOT EXISTS collections
(
    id      UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL REFERENCES users (id),
    name    TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS index_collections_user_id ON collections (user_id);

-- now user can add to their collection an application of another user
-- server must check consistency
CREATE TABLE IF NOT EXISTS collection_applications
(
    collection_id  UUID NOT NULL REFERENCES collections (id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES applications (id) ON DELETE CASCADE,

    PRIMARY KEY (collection_id, application_id)
);


CREATE TABLE IF NOT EXISTS play_sessions
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),
    -- Application already have user_id
    -- user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    application_id UUID        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at    TIMESTAMPTZ,
    CONSTRAINT ps_finished_at_rule CHECK ( finished_at IS NULL OR finished_at >= started_at )
);
CREATE UNIQUE INDEX IF NOT EXISTS unique_play_session_application_id
    ON play_sessions (application_id)
    WHERE finished_at IS NULL;

CREATE TABLE IF NOT EXISTS friendships
(
    id           UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id_from UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_id_to   UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at  TIMESTAMPTZ,

    CONSTRAINT f_different_users CHECK ( user_id_from != user_id_to ),
    CONSTRAINT f_accepted_after_created CHECK ( accepted_at >= created_at )
);
CREATE UNIQUE INDEX IF NOT EXISTS unique_friendship ON friendships
    (
     least(user_id_from, user_id_to),
     greatest(user_id_from, user_id_to)
        );

CREATE TABLE IF NOT EXISTS chats
(
    id                  UUID PRIMARY KEY     DEFAULT uuidv7(),

    user_a_id           UUID        NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    user_b_id           UUID        NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT c_different_users CHECK (user_a_id <> user_b_id),

    user_a_last_read_at TIMESTAMPTZ,
    user_b_last_read_at TIMESTAMPTZ,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS unique_users_pairs
    ON chats (
              least(user_a_id, user_b_id),
              greatest(user_a_id, user_b_id)
        );

CREATE INDEX IF NOT EXISTS chat_user_a_index
    ON chats (user_a_id);
CREATE INDEX IF NOT EXISTS chat_user_a_index
    ON chats (user_b_id);

CREATE TABLE IF NOT EXISTS messages
(
    id        UUID PRIMARY KEY     DEFAULT uuidv7(),
    chat_id   UUID        NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    author_id UUID        NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    content   TEXT        NOT NULL
        CONSTRAINT m_content_l CHECK ( char_length(content) <= 1000 ),

    sent_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS message_chat_id_index ON messages (chat_id)