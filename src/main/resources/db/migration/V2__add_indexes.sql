
create index if not exists ix_app_match   on appearances(match_id);
create index if not exists ix_app_player  on appearances(player_id);
create index if not exists ix_player_team on players(team_id);


