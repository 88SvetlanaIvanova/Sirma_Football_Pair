CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE teams (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  legacy_id        bigint,
  name             text NOT NULL,
  manager_full_name text NOT NULL,
  group_name       text NOT NULL
);

CREATE TABLE players (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  legacy_id    bigint,
  team_id      uuid NOT NULL REFERENCES teams(id),
  team_number  int NOT NULL CHECK (team_number >= 1),
  position     text NOT NULL,
  full_name    text NOT NULL
);


CREATE TABLE matches (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  legacy_id     bigint,
  a_team_id     uuid NOT NULL REFERENCES teams(id),
  b_team_id     uuid NOT NULL REFERENCES teams(id),
  date          date NOT NULL,
  score         text NOT NULL,
  base_duration int NOT NULL DEFAULT 90 CHECK (base_duration >= 0)
);

CREATE TABLE appearances (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  legacy_id    bigint,
  player_id    uuid NOT NULL REFERENCES players(id),
  match_id     uuid NOT NULL REFERENCES matches(id),
  from_minute  int  NOT NULL CHECK (from_minute >= 0),
  to_minute    int,
  CONSTRAINT ck_minute_range
    CHECK (to_minute IS NULL OR to_minute > from_minute)
);

CREATE INDEX IF NOT EXISTS ix_player_team       ON players(team_id);
CREATE INDEX IF NOT EXISTS ix_match_team_a      ON matches(a_team_id);
CREATE INDEX IF NOT EXISTS ix_match_team_b      ON matches(b_team_id);
CREATE INDEX IF NOT EXISTS ix_app_player        ON appearances(player_id);
CREATE INDEX IF NOT EXISTS ix_app_match         ON appearances(match_id);

CREATE INDEX IF NOT EXISTS ix_teams_legacy      ON teams(legacy_id);
CREATE INDEX IF NOT EXISTS ix_players_legacy    ON players(legacy_id);
CREATE INDEX IF NOT EXISTS ix_matches_legacy    ON matches(legacy_id);
CREATE INDEX IF NOT EXISTS ix_appearances_legacy ON appearances(legacy_id);


CREATE UNIQUE INDEX IF NOT EXISTS ux_teams_name_group ON teams(name, group_name);