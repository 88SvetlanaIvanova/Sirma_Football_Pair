alter table players add constraint ux_players_legacy unique (legacy_id);
alter table matches add constraint ux_matches_legacy unique (legacy_id);
create unique index if not exists ux_teams_name_group on teams (name, group_name);
alter table appearances add constraint ux_appearances_legacy unique (legacy_id);