package com.sirma.football.repositorytest;

import com.sirma.football.repositories.PlayerRepository;
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

public class PlayerRepositoryAfterImportIT {

    @Autowired CsvImportService importer;
    @Autowired PlayerRepository repo;

    @Test
    @DisplayName("findByLegacyId returns player after CSV import")
    void findsByLegacyId_afterImport() throws Exception {
        try (var teamsReader   = new InputStreamReader(new ClassPathResource("data/teams.csv").getInputStream(), StandardCharsets.UTF_8);
             var playersReader = new InputStreamReader(new ClassPathResource("data/players.csv").getInputStream(), StandardCharsets.UTF_8)) {
            importer.importTeams(teamsReader);
            importer.importPlayers(playersReader);
        }

        var player = repo.findByLegacyId(1L);
        assertThat(player).isPresent();
    }

}
