CREATE INDEX IF NOT EXISTS ix_app_match   ON appearances(match_id);
CREATE INDEX IF NOT EXISTS ix_app_player  ON appearances(player_id);
CREATE INDEX IF NOT EXISTS ix_player_team ON players(team_id);

UPDATE matches SET base_duration = 90 WHERE base_duration IS NULL;

ALTER TABLE matches ALTER COLUMN base_duration SET DEFAULT 90;
ALTER TABLE matches ALTER COLUMN base_duration SET NOT NULL;

ALTER TABLE matches
  ADD CONSTRAINT ck_matches_base_duration_range
  CHECK (base_duration BETWEEN 0 AND 150);

CREATE UNIQUE INDEX IF NOT EXISTS ux_teams_name_group ON teams (name, group_name);