package com.sirma.football.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.football.controller.MatchController;
import com.sirma.football.model.dto.MatchCreateDto;
import com.sirma.football.model.dto.MatchDto;
import com.sirma.football.services.MatchCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchControllerTest {

    private MockMvc mvc;
    private MatchCrudService svc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setup() {
        svc = Mockito.mock(MatchCrudService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new MatchController(svc))
                .build();
    }

    private MatchDto matchDto(UUID id, UUID home, UUID away) {
        return new MatchDto(id, home, away, LocalDate.of(2024, 6, 14), "2-1", 90);
    }

    private MatchCreateDto matchCreateDto(UUID home, UUID away) {
        return new MatchCreateDto(home, away, LocalDate.of(2024, 6, 14), "2-1", 90);
    }

    @Test
    void list_all_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID h = UUID.randomUUID();
        UUID a = UUID.randomUUID();

        given(svc.list(null)).willReturn(List.of(matchDto(id, h, a)));

        mvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].score").value("2-1"));
    }

    @Test
    void list_byTeam_returns200() throws Exception {
        UUID team = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID h = team;
        UUID a = UUID.randomUUID();

        given(svc.list(team)).willReturn(List.of(matchDto(id, h, a)));

        mvc.perform(get("/api/matches").param("teamId", team.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamAId").value(team.toString()));
    }

    @Test
    void get_byId_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID h = UUID.randomUUID();
        UUID a = UUID.randomUUID();

        given(svc.get(id)).willReturn(matchDto(id, h, a));

        mvc.perform(get("/api/matches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.baseDuration").value(90));
    }

    @Test
    void create_returns201_andLocation() throws Exception {
        UUID id = UUID.randomUUID();
        UUID h = UUID.randomUUID();
        UUID a = UUID.randomUUID();

        MatchCreateDto create = matchCreateDto(h, a);
        MatchDto created = matchDto(id, h, a);

        given(svc.create(any(MatchCreateDto.class), isNull())).willReturn(created);

        mvc.perform(post("/api/matches")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/matches/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID h = UUID.randomUUID();
        UUID a = UUID.randomUUID();

        MatchCreateDto update = matchCreateDto(h, a);
        MatchDto updated = matchDto(id, h, a);

        given(svc.update(id, update)).willReturn(updated);

        mvc.perform(put("/api/matches/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void delete_returns200_andMessage() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/matches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Match " + id + " deleted successfully"));

        verify(svc).delete(id);
    }
}
