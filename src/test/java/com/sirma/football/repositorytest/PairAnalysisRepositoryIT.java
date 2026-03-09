package com.sirma.football.repositorytest;

import com.sirma.football.model.util.PairResultDto;
import com.sirma.football.repositories.PairAnalysisRepository;
import com.sirma.football.services.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class PairAnalysisRepositoryIT {

    @Autowired
    CsvImportService importer;

    @Autowired
    PairAnalysisRepository repo;

    @Test
    void shouldFindTopPairFromCsv() throws Exception {

        importer.importTeams(new InputStreamReader(new ClassPathResource("data/teams.csv").getInputStream()));
        importer.importPlayers(new InputStreamReader(new ClassPathResource("data/players.csv").getInputStream()));
        importer.importMatches(new InputStreamReader(new ClassPathResource("data/matches.csv").getInputStream()));
        importer.importAppearances(new InputStreamReader(new ClassPathResource("data/records.csv").getInputStream()));

        Optional<PairResultDto> result = repo.findTopPair();
        System.out.println("Top pair result: " + result);
        assertTrue(result.isPresent());
        assertEquals(113L, result.get().p1());
        assertEquals(128L, result.get().p2());
        assertEquals(84, result.get().totalMinutes());
    }
}