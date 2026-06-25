/*
-- V2: add useful indexes and remove redundant ones
-- Remove previously created redundant indexes (if present)
DROP INDEX IF EXISTS exercise_id;
DROP INDEX IF EXISTS exercise_tips_id;

-- Users: speed up case-insensitive name search
CREATE INDEX IF NOT EXISTS idx_users_lower_name ON users (lower(name));

-- Foreign key indexes for faster joins/filters
CREATE INDEX IF NOT EXISTS idx_workout_plans_user_id ON workout_plans (user_id);
CREATE INDEX IF NOT EXISTS idx_workout_days_workout_plan_id ON workout_days (workout_plan_id);
CREATE INDEX IF NOT EXISTS idx_workout_day_exercises_workout_day_id ON workout_day_exercises (workout_day_id);
CREATE INDEX IF NOT EXISTS idx_workout_day_exercises_exercise_id ON workout_day_exercises (exercise_id);
CREATE INDEX IF NOT EXISTS idx_muscles_activation_exercise_id ON muscles_activation (exercise_id);
CREATE INDEX IF NOT EXISTS idx_exercise_tips_exercise_id ON exercise_tips (exercise_id);
CREATE INDEX IF NOT EXISTS idx_exercise_contraindications_exercise_id ON exercise_contraindications (exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_id ON workout_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started_at ON workout_sessions (user_id, started_at);
CREATE INDEX IF NOT EXISTS idx_workout_session_exercises_workout_session_id ON workout_session_exercises (workout_session_id);
CREATE INDEX IF NOT EXISTS idx_workout_session_exercises_exercise_id ON workout_session_exercises (exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_session_sets_exercise_id ON workout_session_sets (workout_session_exercise_id);

-- Helpful filters
CREATE INDEX IF NOT EXISTS idx_exercises_active ON exercises (active);
CREATE INDEX IF NOT EXISTS idx_workout_plans_user_active ON workout_plans (user_id, active);
