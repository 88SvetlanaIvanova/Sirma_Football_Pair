package com.sirma.football.repositories;

import com.sirma.football.model.util.PairMatchDto;
import com.sirma.football.model.util.PairResultDto;
import com.sirma.football.mappers.RowMappers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PairAnalysisRepository {
    private final JdbcTemplate jdbcTemplate;

    public PairAnalysisRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BY_MATCH_CTE = """
    with resolved as (
      select
        ap.id,
        ap.player_id,
        p.legacy_id      as p_legacy,
        p.team_id,
        ap.match_id,
        m.legacy_id      as match_legacy,
        ap.from_minute   as from_m,
        coalesce(ap.to_minute, coalesce(m.base_duration, 90)) as to_m
      from appearances ap
      join matches   m on m.id = ap.match_id
      join players   p on p.id = ap.player_id
    ),
    pair_overlap as (
      select
        a.p_legacy     as p1_legacy,
        b.p_legacy     as p2_legacy,
        a.match_legacy as match_legacy,
        greatest(0, least(a.to_m, b.to_m) - greatest(a.from_m, b.from_m)) as overlap
      from resolved a
      join resolved b
        on a.match_id = b.match_id
       and a.team_id  = b.team_id
       and a.player_id < b.player_id
       and a.to_m > b.from_m
       and b.to_m > a.from_m
    )
    select
      cast(p1_legacy as bigint)            as p1,
      cast(p2_legacy as bigint)            as p2,
      cast(match_legacy as bigint)         as match_id,
      cast(sum(overlap) as int)            as minutes_together
    from pair_overlap
    group by p1_legacy, p2_legacy, match_legacy
    having sum(overlap) > 0
    """;

    public List<PairMatchDto> findByMatchForPair(Long p1Legacy, Long p2Legacy) {
        String sql = """
            with by_match as (
              %s
            )
            select p1, p2, match_id, minutes_together
            from by_match
            where (p1 = ? and p2 = ?) or (p1 = ? and p2 = ?)
            order by match_id
            """.formatted(BY_MATCH_CTE);

        return jdbcTemplate.query(sql, RowMappers::pairMatchDto, p1Legacy, p2Legacy, p1Legacy, p2Legacy);
    }


    public Optional<PairResultDto> findTopPair() {
        String sql = """
            with by_match as (
              %s
            )
            select
              p1,
              p2,
              cast(sum(minutes_together) as int) as total_minutes
            from by_match
            group by p1, p2
            order by total_minutes desc
            limit 1
            """.formatted(BY_MATCH_CTE);

        var list = jdbcTemplate.query(sql, RowMappers::pairResultDto);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }
}