package com.sirma.football.repositorytest;

import com.sirma.football.entities.Match;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.MatchRepository;
import com.sirma.football.services.CsvImportService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CsvImportService.class)
class MatchRepositoryIT {

    @Autowired CsvImportService importer;
    @Autowired MatchRepository repo;
    @Autowired AppearanceRepository appearanceRepository;

    void loadData() throws Exception {
        importer.importTeams(new InputStreamReader(new ClassPathResource("data/teams.csv").getInputStream()));
        importer.importPlayers(new InputStreamReader(new ClassPathResource("data/players.csv").getInputStream()));
        importer.importMatches(new InputStreamReader(new ClassPathResource("data/matches.csv").getInputStream()));
        importer.importAppearances(new InputStreamReader(new ClassPathResource("data/records.csv").getInputStream()));
    }
    @Test
    @DisplayName("findByLegacyId returns match after CSV import")
    void shouldFindMatchByLegacyId() throws Exception {
        try (var teamsReader   = new InputStreamReader(new ClassPathResource("data/teams.csv").getInputStream(), StandardCharsets.UTF_8);
             var matchesReader = new InputStreamReader(new ClassPathResource("data/matches.csv").getInputStream(), StandardCharsets.UTF_8)) {
            importer.importTeams(teamsReader);
            importer.importMatches(matchesReader);
        }

        var match = repo.findByLegacyId(1L);

        assertThat(match).isPresent();
        assertThat(match.get().getLegacyId()).isEqualTo(1L);
    }
    @Test
    void shouldCheckMatchExistsForTeam() throws Exception {

        loadData();

        Match firstMatch = repo.findAll().getFirst();

        UUID teamId = firstMatch.getTeamA().getId();

        boolean exists = repo.existsByTeamA_IdOrTeamB_Id(teamId, teamId);

        assertTrue(exists);
    }

    @Test
    void shouldFindMatchesForTeam() throws Exception {

        loadData();

        Match firstMatch = repo.findAll().getFirst();
        UUID teamId = firstMatch.getTeamA().getId();

        List<Match> matches =
                repo.findByTeamA_IdOrTeamB_Id(teamId, teamId);

        assertFalse(matches.isEmpty());
    }

    @Test
    void shouldDeleteMatchesByTeamId() throws Exception {

        loadData();

        Match firstMatch = repo.findAll().getFirst();
        UUID teamId = firstMatch.getTeamA().getId();

        int deletedApps = appearanceRepository.deleteByMatchesOfTeam(teamId);

        int deleted = repo.deleteByTeamId(teamId);

        assertTrue(deleted > 0);
    }
}