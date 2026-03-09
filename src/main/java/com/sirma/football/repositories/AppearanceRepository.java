package com.sirma.football.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sirma.football.entities.Appearance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppearanceRepository extends JpaRepository<Appearance, UUID> {

    @Query("""
      select a
      from Appearance a
      join fetch a.player p
      join fetch p.team t
      where a.match.id = :matchId
    """)

    List<Appearance> findAllWithPlayersByMatch(@Param("matchId") UUID matchId);

    @Modifying
    @Transactional
    @Query("delete from Appearance a where a.player.id = :playerId")
    void deleteByPlayerId(@Param("playerId") UUID playerID);

    @Modifying
    @Transactional
    @Query("""
           delete from Appearance a
           where a.player.id in (
               select p.id from Player p where p.team.id = :teamId
           )
           """)
    void deleteByTeamId(@Param("teamId") UUID teamId);

    @Modifying
    @Transactional
    @Query("delete from Appearance a where a.match.id in (select m.id from Match m where m.teamA.id = :teamId or m.teamB.id = :teamId)")
    int deleteByMatchesOfTeam(@Param("teamId") UUID teamId);

    @Modifying
    @Transactional
    @Query("delete from Appearance a where a.match.id = :matchId")
    void deleteByMatchId(@Param("matchId") UUID matchId);

}
