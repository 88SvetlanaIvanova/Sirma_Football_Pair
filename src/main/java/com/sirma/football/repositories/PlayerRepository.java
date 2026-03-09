package com.sirma.football.repositories;

import com.sirma.football.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    List<Player> findByTeamId(UUID teamId);
    Optional<Player> findByLegacyId(Long legacyId);

    @Modifying @Transactional
    @Query("delete from Player p where p.team.id = :teamId")
    int deleteByTeamId(@Param("teamId") UUID teamId);

    boolean existsByTeam_Id(UUID teamId);

    List<Player> findByTeam_Name(String teamName);
}
