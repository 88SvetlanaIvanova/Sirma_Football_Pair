package com.sirma.football.controllertest;

import com.sirma.football.controller.ImportController;
import com.sirma.football.services.CsvImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ImportControllerTest {

    private MockMvc mvc;
    private CsvImportService service;

    @BeforeEach
    void setup() {
        service = Mockito.mock(CsvImportService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new ImportController(service))
                .build();
    }

    @Test
    void debug_returnsInfo_aboutUploadedFile() throws Exception {
        byte[] content = "id;name\n1;Spain".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "teams.csv", "text/csv", content);

        mvc.perform(multipart("/api/import/debug").file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multipart").value(true))
                .andExpect(jsonPath("$.fileName").value("teams.csv"))
                .andExpect(jsonPath("$.fileSize").value(content.length))
                .andExpect(jsonPath("$.fileContentType").value("text/csv"));
    }

    @Test
    void importTeams_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "teams.csv", "text/csv", "id;name\n1;Spain".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/import/teams").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void importPlayers_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "players.csv", "text/csv", "teamId;num;pos;name\n...".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/import/players").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void importMatches_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "matches.csv", "text/csv", "a;b;date;score;base\n...".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/import/matches").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void importAppearances_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "appearances.csv", "text/csv", "matchId;playerId;minutes\n...".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/import/appearances").file(file))
                .andExpect(status().isOk());
    }
}
