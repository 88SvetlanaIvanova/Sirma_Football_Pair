CREATE TABLE teams (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  legacy_id BIGINT,
  name VARCHAR(255) NOT NULL,
  manager_full_name VARCHAR(255) NOT NULL,
  group_name VARCHAR(255) NOT NULL
);

CREATE TABLE players (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  legacy_id BIGINT,
  team_id UUID NOT NULL REFERENCES teams(id),
  team_number INT NOT NULL CHECK (team_number >= 1),
  position VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL
);

CREATE TABLE matches (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  legacy_id BIGINT,
  a_team_id UUID NOT NULL REFERENCES teams(id),
  b_team_id UUID NOT NULL REFERENCES teams(id),
  date DATE NOT NULL,
  score VARCHAR(255) NOT NULL,
  base_duration INT NOT NULL DEFAULT 90 CHECK (base_duration >= 0)
);

CREATE TABLE appearances (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  legacy_id BIGINT,
  player_id UUID NOT NULL REFERENCES players(id),
  match_id UUID NOT NULL REFERENCES matches(id),
  from_minute INT NOT NULL CHECK (from_minute >= 0),
  to_minute INT,
  CONSTRAINT ck_minute_range CHECK (to_minute IS NULL OR to_minute > from_minute)
);