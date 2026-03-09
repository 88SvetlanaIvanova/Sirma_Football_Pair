package com.sirma.football.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.football.controller.PlayerController;
import com.sirma.football.model.dto.PlayerCreateDto;
import com.sirma.football.model.dto.PlayerDto;
import com.sirma.football.services.PlayerCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

class PlayerControllerTest {

    private MockMvc mvc;
    private PlayerCrudService svc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setup() {
        svc = Mockito.mock(PlayerCrudService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new PlayerController(svc))
                .build();
    }

    private PlayerDto playerDto(UUID id, UUID teamId) {
        return new PlayerDto(id, teamId, 10, "FW", "Fernando Torres");
    }
    private PlayerCreateDto playerCreateDto(UUID teamId) {
        return new PlayerCreateDto(teamId, 10, "FW", "Fernando Torres");
    }

    @Test
    void list_allPlayers_returns200() throws Exception {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID t = UUID.randomUUID();

        given(svc.list(null)).willReturn(List.of(playerDto(p1, t), playerDto(p2, t)));

        mvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(p1.toString()))
                .andExpect(jsonPath("$[1].id").value(p2.toString()));
    }

    @Test
    void list_byTeam_returns200() throws Exception {
        UUID t = UUID.randomUUID();
        UUID p = UUID.randomUUID();
        given(svc.list(t)).willReturn(List.of(playerDto(p, t)));

        mvc.perform(get("/api/players").param("teamId", t.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamId").value(t.toString()));
    }

    @Test
    void get_byId_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID t = UUID.randomUUID();

        given(svc.get(id)).willReturn(playerDto(id, t));

        mvc.perform(get("/api/players/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.position").value("FW"));
    }

    @Test
    void searchByTeamName_returns200() throws Exception {
        String name = "Spain";
        UUID t = UUID.randomUUID();
        UUID p = UUID.randomUUID();

        given(svc.getPlayersByTeamName(name)).willReturn(List.of(playerDto(p, t)));

        mvc.perform(get("/api/players/by-team").param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(p.toString()));
    }

    @Test
    void create_returns201_andLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        PlayerCreateDto create = playerCreateDto(teamId);
        PlayerDto created = playerDto(id, teamId);

        given(svc.create(any(PlayerCreateDto.class), isNull())).willReturn(created);

        mvc.perform(post("/api/players")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/players/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.teamId").value(teamId.toString()));
    }

    @Test
    void update_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        PlayerCreateDto update = playerCreateDto(teamId);
        PlayerDto updated = playerDto(id, teamId);

        given(svc.update(id, update)).willReturn(updated);

        mvc.perform(put("/api/players/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("Fernando Torres"));
    }

    @Test
    void delete_returns200_andMessage() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/players/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Player " + id + " deleted successfully"));

        verify(svc).delete(id);
    }
}
