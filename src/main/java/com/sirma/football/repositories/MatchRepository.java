package com.sirma.football.repositories;

import com.sirma.football.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByTeamA_IdOrTeamB_Id(UUID teamAId, UUID teamBId);
    Optional<Match> findByLegacyId(Long legacyId);

    @Modifying
    @Transactional
    @Query("delete from Match m where m.teamA.id = :teamId or m.teamB.id = :teamId")
    int deleteByTeamId(@Param("teamId") UUID teamId);

    boolean existsByTeamA_IdOrTeamB_Id(UUID teamAId, UUID teamBId);
}
