package com.sirma.football.repositorytest;

import com.sirma.football.repositories.TeamRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CsvImportService.class)
class TeamRepositoryIT {

    @Autowired CsvImportService importer;
    @Autowired TeamRepository repo;

    void loadTeams() throws Exception {
        try (var teamsReader = new InputStreamReader(
                new ClassPathResource("data/teams.csv").getInputStream(),
                StandardCharsets.UTF_8)) {
            importer.importTeams(teamsReader);
        }
    }

    @Test
    @DisplayName("findByLegacyId returns team after CSV import")
    void shouldFindTeamByLegacyId() throws Exception {

        loadTeams();

        var team = repo.findByLegacyId(1L);

        assertThat(team).isPresent();
        assertThat(team.get().getLegacyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAll returns non-empty list after CSV import")
    void shouldFindAllTeamsAfterCsvImport() throws Exception {
        loadTeams();

        var all = repo.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Repeated import should be idempotent if CSV importer handles duplicates")
    void shouldNotDuplicateOnReimport_ifImporterIsIdempotent() throws Exception {
        loadTeams();
        var countAfterFirst = repo.count();

        loadTeams();
        var countAfterSecond = repo.count();

        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }
}
