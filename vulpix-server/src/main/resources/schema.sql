CREATE TABLE IF NOT EXISTS users
(
    id                      UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username                VARCHAR(30)  NOT NULL,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255), -- I guess it can be null if we will have Google oauth

    avatar_img              UUID,         -- file name

    -- current_play_session_id UUID REFERENCES play_sessions (id),

    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deactivated_at          TIMESTAMPTZ  NULL,
    last_login_at           TIMESTAMPTZ
);


CREATE TABLE IF NOT EXISTS sessions
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users (id),
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    device_info   VARCHAR      NOT NULL,

    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ
);


CREATE TABLE IF NOT EXISTS applications
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    name           VARCHAR(255) NOT NULL,
    played_minutes INT          NOT NULL DEFAULT 0,

    background_img UUID,
    logo_img       UUID,
    icon_img       UUID,
    cover_img      UUID,


    -- don't show it for other users
    is_private     BOOLEAN      NOT NULL DEFAULT FALSE,

    added_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS index_applications_user_id ON applications (user_id);


CREATE TABLE IF NOT EXISTS application_specifications
(
    application_id   UUID PRIMARY KEY REFERENCES applications (id) ON DELETE CASCADE,

    -- we must check if executable exists on this path when the app or game are launched
    -- if not mark the specification as outdated and ask user to update the path.
    application_path VARCHAR,
    proton_version   VARCHAR,
    wine_prefix      VARCHAR,
    launch_options   VARCHAR
);


CREATE TABLE IF NOT EXISTS collections
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID         NOT NULL REFERENCES users (id),
    name    VARCHAR(100) NOT NULL
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
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    application_id UUID        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at    TIMESTAMPTZ
        CHECK ( finished_at IS NULL OR finished_at >= started_at )
);
ALTER TABLE users ADD COLUMN IF NOT EXISTS current_play_session_id UUID REFERENCES play_sessions(id);

CREATE TABLE IF NOT EXISTS friendships
(
    user_id_from UUID REFERENCES users (id) ON DELETE CASCADE,
    user_id_to   UUID REFERENCES users (id) ON DELETE CASCADE,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at  TIMESTAMPTZ,

    PRIMARY KEY (user_id_from, user_id_to),

    CHECK ( user_id_from != user_id_to )
);


CREATE TABLE IF NOT EXISTS chats
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id_from    UUID REFERENCES users (id) ON DELETE CASCADE,
    user_id_to      UUID REFERENCES users (id) ON DELETE CASCADE,

    unread_messages INT         NOT NULL DEFAULT 0,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS unique_friendship ON friendships
    (
     least(user_id_from, user_id_to),
     greatest(user_id_from, user_id_to)
        );


CREATE TABLE IF NOT EXISTS messages
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id   UUID REFERENCES chats (id) ON DELETE CASCADE,
    author_id UUID REFERENCES users (id),

    content   VARCHAR(500) NOT NULL
)