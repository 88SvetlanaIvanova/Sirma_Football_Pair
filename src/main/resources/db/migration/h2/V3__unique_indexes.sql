CREATE UNIQUE INDEX IF NOT EXISTS ux_teams_name_group ON teams (name, group_name);

ALTER TABLE players DROP CONSTRAINT IF EXISTS ux_players_legacy;
ALTER TABLE players ADD CONSTRAINT ux_players_legacy UNIQUE (legacy_id);

ALTER TABLE matches DROP CONSTRAINT IF EXISTS ux_matches_legacy;
ALTER TABLE matches ADD CONSTRAINT ux_matches_legacy UNIQUE (legacy_id);

ALTER TABLE appearances DROP CONSTRAINT IF EXISTS ux_appearances_legacy;
ALTER TABLE appearances ADD CONSTRAINT ux_appearances_legacy UNIQUE (legacy_id);