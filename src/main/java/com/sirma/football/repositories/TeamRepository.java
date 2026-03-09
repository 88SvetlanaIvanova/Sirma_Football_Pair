package com.sirma.football.repositories;

import com.sirma.football.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByLegacyId(Long legacyId);
}

