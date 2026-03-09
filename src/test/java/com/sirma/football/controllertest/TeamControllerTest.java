package com.sirma.football.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.football.controller.TeamController;
import com.sirma.football.model.dto.TeamCreateDto;
import com.sirma.football.model.dto.TeamDto;
import com.sirma.football.services.TeamCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TeamControllerTest {

    private MockMvc mvc;
    private TeamCrudService svc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        svc = Mockito.mock(TeamCrudService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new TeamController(svc))
                .build();
    }

    private TeamDto teamDto(UUID id) {
        return new TeamDto(id, "Spain", "Luis de la Fuente", "A");
    }
    private TeamCreateDto teamCreateDto() {
        return new TeamCreateDto("Spain", "Luis de la Fuente", "A");
    }

    @Test
    void list_returns200_andJsonArray() throws Exception {
        UUID id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
        given(svc.list()).willReturn(List.of(teamDto(id1), teamDto(id2)));

        mvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[1].id").value(id2.toString()));
    }

    @Test
    void create_returns201_andLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        given(svc.create(any(TeamCreateDto.class), isNull())).willReturn(teamDto(id));

        mvc.perform(post("/api/teams")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(teamCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/teams/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}