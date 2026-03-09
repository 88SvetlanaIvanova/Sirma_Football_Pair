update matches set base_duration = 90 where base_duration is null;

alter table matches alter column base_duration set default 90;

alter table matches alter column base_duration set not null;

alter table matches
  add constraint ck_matches_base_duration_range
  check (base_duration between 0 and 150);