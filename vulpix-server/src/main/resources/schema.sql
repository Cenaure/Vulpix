CREATE TABLE IF NOT EXISTS users
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),

    username       TEXT        NOT NULL,
    CONSTRAINT u_username_l CHECK (char_length(username) BETWEEN 2 AND 100),

    email          TEXT        NOT NULL, -- not unique because must be unique only for not deactivated accounts
    CONSTRAINT u_email_l CHECK (char_length(email) <= 254),

    password_hash  TEXT,                 -- I guess it can be null if we will have Google oauth

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

    added_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS index_applications_user_id ON applications (user_id);


CREATE TABLE IF NOT EXISTS application_specifications
(
    application_id   UUID PRIMARY KEY REFERENCES applications (id) ON DELETE CASCADE,

    background_img   UUID,
    logo_img         UUID,
    icon_img         UUID,
    cover_img        UUID,

    -- don't show it for other users
    is_private       BOOLEAN NOT NULL DEFAULT FALSE,

    -- we must check if executable exists on this path when the app or game are launched
    -- if not mark the specification as outdated and ask user to update the path.
    application_path TEXT,
    proton_version   TEXT,
    wine_prefix      TEXT,
    launch_options   TEXT
);


CREATE TABLE IF NOT EXISTS collections
(
    id      UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL REFERENCES users (id),
    name    TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS index_collections_user_id ON collections (user_id);


CREATE TABLE IF NOT EXISTS collection_applications
(
    collection_id  UUID NOT NULL REFERENCES collections (id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES applications (id) ON DELETE CASCADE,

    PRIMARY KEY (collection_id, application_id)
);


CREATE TABLE IF NOT EXISTS play_sessions
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    application_id UUID        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at    TIMESTAMPTZ,
    CONSTRAINT ps_finished_at_rule CHECK ( finished_at IS NULL OR finished_at >= started_at )
);
CREATE UNIQUE INDEX IF NOT EXISTS unique_play_session_user_id
    ON play_sessions (user_id)
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
    id              UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id_from    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_id_to      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    unread_messages INT         NOT NULL DEFAULT 0,
    CONSTRAINT c_positive_unread_messages CHECK ( unread_messages >= 0 ),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chat_members
(
    user_id      UUID REFERENCES users (id) ON DELETE CASCADE,
    chat_id      UUID REFERENCES chats (id) ON DELETE CASCADE,
    last_read_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS messages
(
    id        UUID PRIMARY KEY     DEFAULT uuidv7(),
    chat_id   UUID        NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    author_id UUID        NOT NULL REFERENCES users (id),

    content   TEXT        NOT NULL
        CONSTRAINT m_content_l CHECK ( char_length(content) <= 1000 ),

    sent_at   TIMESTAMPTZ NOT NULL DEFAULT now()
)