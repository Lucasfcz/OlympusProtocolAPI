CREATE TABLE users
(
    id               UUID                        NOT NULL,
    name             VARCHAR(255)                NOT NULL,
    email            VARCHAR(255)                NOT NULL,
    password         VARCHAR(255)                NOT NULL,
    role             VARCHAR(255)                NOT NULL,
    avatar_url       VARCHAR(255),
    experience_level VARCHAR(255)                NOT NULL,
    body_weight      DOUBLE PRECISION,
    height           DOUBLE PRECISION,
    active           BOOLEAN                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE TABLE workout_plans
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    goal       VARCHAR(255),
    is_public  BOOLEAN                     NOT NULL,
    active     BOOLEAN                     NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_workout_plans PRIMARY KEY (id)
);

ALTER TABLE workout_plans
    ADD CONSTRAINT FK_WORKOUT_PLANS_ON_USER
        FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE workout_days
(
    id              UUID         NOT NULL,
    workout_plan_id UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    day_order       INTEGER      NOT NULL,
    CONSTRAINT pk_workout_days PRIMARY KEY (id)
);

ALTER TABLE workout_days
    ADD CONSTRAINT FK_WORKOUT_DAYS_ON_WORKOUT_PLAN
        FOREIGN KEY (workout_plan_id) REFERENCES workout_plans (id);

CREATE TABLE exercises
(
    id                           UUID         NOT NULL,
    name                         VARCHAR(255) NOT NULL,
    description                  VARCHAR(1000),
    recommended_experience_level VARCHAR(255) NOT NULL,
    safety_rating                VARCHAR(255) NOT NULL,
    efficiency_rating            VARCHAR(255) NOT NULL,
    admin_notes                  VARCHAR(1000),
    gif_url                      VARCHAR(255),
    uses_body_weight             BOOLEAN      NOT NULL,
    active                       BOOLEAN      NOT NULL,
    create_at                    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_exercises PRIMARY KEY (id)
);

CREATE TABLE exercise_tips
(
    id           UUID         NOT NULL,
    exercise_id  UUID         NOT NULL,
    target_level VARCHAR(255) NOT NULL,
    tip_type     VARCHAR(255) NOT NULL,
    content      VARCHAR(500) NOT NULL,
    CONSTRAINT pk_exercise_tips PRIMARY KEY (id)
);

ALTER TABLE exercise_tips
    ADD CONSTRAINT FK_EXERCISE_TIPS_ON_EXERCISE
        FOREIGN KEY (exercise_id) REFERENCES exercises (id);

CREATE TABLE exercise_contraindications
(
    id          UUID         NOT NULL,
    exercise_id UUID         NOT NULL,
    condition   VARCHAR(255) NOT NULL,
    explanation VARCHAR(500) NOT NULL,
    CONSTRAINT pk_exercise_contraindications PRIMARY KEY (id)
);

ALTER TABLE exercise_contraindications
    ADD CONSTRAINT FK_EXERCISE_CONTRAINDICATIONS_ON_EXERCISE
        FOREIGN KEY (exercise_id) REFERENCES exercises (id);

CREATE TABLE muscles_activation
(
    id                 UUID         NOT NULL,
    exercise_id        UUID         NOT NULL,
    muscle_group       VARCHAR(255) NOT NULL,
    muscle_region      VARCHAR(255),
    muscle_head        VARCHAR(255),
    muscle_role        VARCHAR(255) NOT NULL,
    activation_percent INTEGER      NOT NULL,
    CONSTRAINT pk_muscles_activation PRIMARY KEY (id)
);

ALTER TABLE muscles_activation
    ADD CONSTRAINT FK_MUSCLES_ACTIVATION_ON_EXERCISE
        FOREIGN KEY (exercise_id) REFERENCES exercises (id);

CREATE TABLE workout_day_exercises
(
    id             UUID    NOT NULL,
    workout_day_id UUID    NOT NULL,
    exercise_id    UUID    NOT NULL,
    exercise_order INTEGER NOT NULL,
    sets           INTEGER NOT NULL,
    reps           INTEGER NOT NULL,
    rest_time      INTEGER NOT NULL,
    CONSTRAINT pk_workout_day_exercises PRIMARY KEY (id)
);

ALTER TABLE workout_day_exercises
    ADD CONSTRAINT FK_WORKOUT_DAY_EXERCISES_ON_EXERCISE
        FOREIGN KEY (exercise_id) REFERENCES exercises (id);

ALTER TABLE workout_day_exercises
    ADD CONSTRAINT FK_WORKOUT_DAY_EXERCISES_ON_WORKOUT_DAY
        FOREIGN KEY (workout_day_id) REFERENCES workout_days (id);

CREATE TABLE workout_sessions
(
    id             UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    workout_day_id UUID,
    started_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    finished_at    TIMESTAMP WITHOUT TIME ZONE,
    notes          VARCHAR(500),
    CONSTRAINT pk_workout_sessions PRIMARY KEY (id)
);

ALTER TABLE workout_sessions
    ADD CONSTRAINT FK_WORKOUT_SESSIONS_ON_USER
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE workout_sessions
    ADD CONSTRAINT FK_WORKOUT_SESSIONS_ON_WORKOUT_DAY
        FOREIGN KEY (workout_day_id) REFERENCES workout_days (id);

CREATE TABLE workout_session_exercises
(
    id                 UUID    NOT NULL,
    workout_session_id UUID    NOT NULL,
    exercise_id        UUID    NOT NULL,
    exercise_order     INTEGER NOT NULL,
    CONSTRAINT pk_workout_session_exercises PRIMARY KEY (id)
);

ALTER TABLE workout_session_exercises
    ADD CONSTRAINT FK_WORKOUT_SESSION_EXERCISES_ON_EXERCISE
        FOREIGN KEY (exercise_id) REFERENCES exercises (id);

ALTER TABLE workout_session_exercises
    ADD CONSTRAINT FK_WORKOUT_SESSION_EXERCISES_ON_WORKOUT_SESSION
        FOREIGN KEY (workout_session_id) REFERENCES workout_sessions (id);

CREATE TABLE workout_session_sets
(
    id                          UUID    NOT NULL,
    workout_session_exercise_id UUID    NOT NULL,
    set_order                   INTEGER NOT NULL,
    reps                        INTEGER NOT NULL,
    weight                      DOUBLE PRECISION,
    rest_time                   INTEGER,
    rpe                         DOUBLE PRECISION,
    CONSTRAINT pk_workout_session_sets PRIMARY KEY (id)
);

ALTER TABLE workout_session_sets
    ADD CONSTRAINT FK_WORKOUT_SESSION_SETS_ON_WORKOUT_SESSION_EXERCISE
        FOREIGN KEY (workout_session_exercise_id)
            REFERENCES workout_session_exercises (id);