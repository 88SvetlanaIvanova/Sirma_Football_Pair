package com.sirma.football.servicestests;

import com.sirma.football.entities.Player;
import com.sirma.football.entities.Team;
import com.sirma.football.model.dto.PlayerCreateDto;
import com.sirma.football.model.dto.PlayerDto;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.PlayerRepository;
import com.sirma.football.repositories.TeamRepository;
import com.sirma.football.services.PlayerCrudService;
import com.sirma.football.web.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCrudServiceTest {

    @Mock PlayerRepository players;
    @Mock TeamRepository teams;
    @Mock AppearanceRepository appearances;

    @InjectMocks PlayerCrudService svc;

    private static Team mkTeam(UUID id) {
        Team t = new Team();
        t.setId(id);
        return t;
    }

    private static Player mkPlayer(UUID id, Team team, int number, String pos, String name) {
        Player p = new Player();
        p.setId(id);
        p.setTeam(team);
        p.setTeamNumber(number);
        p.setPosition(pos);
        p.setFullName(name);
        return p;
    }

    @Test
    void create_setsProvidedId_andPersists() {
        UUID teamId = UUID.randomUUID();
        UUID clientProvidedId = UUID.randomUUID();
        Team team = mkTeam(teamId);

        PlayerCreateDto dto = new PlayerCreateDto(teamId, 10, "FW", "Fernando Torres");

        given(teams.findById(teamId)).willReturn(Optional.of(team));
        given(players.save(any(Player.class))).willAnswer(inv -> inv.getArgument(0));

        PlayerDto out = svc.create(dto, clientProvidedId);

        assertNotNull(out);
        assertEquals(clientProvidedId, out.id());
        assertEquals(teamId, out.teamId());
        assertEquals(10, out.teamNumber());
        assertEquals("FW", out.position());
        assertEquals("Fernando Torres", out.fullName());

        verify(teams).findById(teamId);
        verify(players).save(any(Player.class));
        verifyNoMoreInteractions(teams, players, appearances);
    }

    @Test
    void create_generatesIdWhenClientDoesNotProvide() {
        UUID teamId = UUID.randomUUID();
        Team team = mkTeam(teamId);

        PlayerCreateDto dto = new PlayerCreateDto(teamId, 8, "MF", "Andres Iniesta");

        given(teams.findById(teamId)).willReturn(Optional.of(team));
        // Simulate DB-generated ID on save
        given(players.save(any(Player.class))).willAnswer(inv -> {
            Player p = inv.getArgument(0);
            if (p.getId() == null) p.setId(UUID.randomUUID());
            return p;
        });

        PlayerDto out = svc.create(dto, null);

        assertNotNull(out);
        assertNotNull(out.id());
        assertEquals(teamId, out.teamId());
        assertEquals(8, out.teamNumber());
        assertEquals("MF", out.position());
        assertEquals("Andres Iniesta", out.fullName());

        verify(teams).findById(teamId);
        verify(players).save(any(Player.class));
        verifyNoMoreInteractions(teams, players, appearances);
    }

    @Test
    void create_throws_whenTeamNotFound() {
        UUID teamId = UUID.randomUUID();
        PlayerCreateDto dto = new PlayerCreateDto(teamId, 5, "DF", "Carles Puyol");

        given(teams.findById(teamId)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.create(dto, null));
        assertTrue(ex.getMessage().contains("Team " + teamId + " not found"));

        verify(teams).findById(teamId);
        verifyNoMoreInteractions(teams, players, appearances);
    }

    @Test
    void get_returnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        Team team = mkTeam(teamId);
        Player entity = mkPlayer(id, team, 10, "FW", "Fernando Torres");

        given(players.findById(id)).willReturn(Optional.of(entity));

        PlayerDto out = svc.get(id);

        assertEquals(id, out.id());
        assertEquals(teamId, out.teamId());
        assertEquals(10, out.teamNumber());
        assertEquals("FW", out.position());
        assertEquals("Fernando Torres", out.fullName());

        verify(players).findById(id);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void get_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(players.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.get(id));
        assertTrue(ex.getMessage().contains("Player " + id + " not found"));

        verify(players).findById(id);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void list_all_whenTeamIdNull() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID teamA = UUID.randomUUID();
        UUID teamB = UUID.randomUUID();

        Team ta = mkTeam(teamA);
        Team tb = mkTeam(teamB);

        given(players.findAll()).willReturn(List.of(
                mkPlayer(id1, ta, 9, "FW", "Fernando Torres"),
                mkPlayer(id2, tb, 8, "MF", "Andres Iniesta")
        ));

        List<PlayerDto> out = svc.list(null);

        assertEquals(2, out.size());
        assertEquals(id1, out.get(0).id());
        assertEquals(id2, out.get(1).id());

        verify(players).findAll();
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void list_byTeam_whenTeamIdProvided() {
        UUID teamId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Team t = mkTeam(teamId);

        given(players.findByTeamId(teamId)).willReturn(List.of(mkPlayer(id, t, 11, "FW", "Neymar Jr")));

        List<PlayerDto> out = svc.list(teamId);

        assertEquals(1, out.size());
        assertEquals(id, out.get(0).id());
        assertEquals(teamId, out.get(0).teamId());
        verify(players).findByTeamId(teamId);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void getPlayersByTeamName_returnsDtos() {
        UUID teamId = UUID.randomUUID();
        Team t = mkTeam(teamId);
        Player p = mkPlayer(UUID.randomUUID(), t, 7, "FW", "Cristiano Ronaldo");

        given(players.findByTeam_Name("Portugal")).willReturn(List.of(p));

        List<PlayerDto> out = svc.getPlayersByTeamName("Portugal");

        assertEquals(1, out.size());
        assertEquals(teamId, out.getFirst().teamId());
        assertEquals("Cristiano Ronaldo", out.getFirst().fullName());
        verify(players).findByTeam_Name("Portugal");
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void getPlayersByTeamName_emptyListWhenNoMatches() {
        given(players.findByTeam_Name("Nowhere FC")).willReturn(List.of());

        List<PlayerDto> out = svc.getPlayersByTeamName("Nowhere FC");

        assertNotNull(out);
        assertTrue(out.isEmpty());
        verify(players).findByTeam_Name("Nowhere FC");
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void update_modifiesManagedEntity_andReturnsDto() {
        UUID id = UUID.randomUUID();
        UUID oldTeam = UUID.randomUUID();
        UUID newTeam = UUID.randomUUID();

        Team oldT = mkTeam(oldTeam);
        Team newT = mkTeam(newTeam);

        Player existing = mkPlayer(id, oldT, 99, "GK", "Random Keeper");
        PlayerCreateDto dto = new PlayerCreateDto(newTeam, 5, "DF", "Gerard Pique");

        given(players.findById(id)).willReturn(Optional.of(existing));
        given(teams.findById(newTeam)).willReturn(Optional.of(newT));

        PlayerDto out = svc.update(id, dto);

        assertEquals(id, out.id());
        assertEquals(newTeam, out.teamId());
        assertEquals(5, out.teamNumber());
        assertEquals("DF", out.position());
        assertEquals("Gerard Pique", out.fullName());

        verify(players).findById(id);
        verify(teams).findById(newTeam);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void update_throws_whenPlayerNotFound() {
        UUID id = UUID.randomUUID();
        PlayerCreateDto dto = new PlayerCreateDto(UUID.randomUUID(), 3, "DF", "John Doe");

        given(players.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(id, dto));
        assertTrue(ex.getMessage().contains("Player " + id + " not found"));

        verify(players).findById(id);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void update_throws_whenTeamNotFound() {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        PlayerCreateDto dto = new PlayerCreateDto(teamId, 6, "MF", "Xavi Hernandez");

        given(players.findById(id)).willReturn(Optional.of(mkPlayer(id, mkTeam(UUID.randomUUID()), 10, "FW", "Temp")));
        given(teams.findById(teamId)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(id, dto));
        assertTrue(ex.getMessage().contains("Team " + teamId + " not found"));

        verify(players).findById(id);
        verify(teams).findById(teamId);
        verifyNoMoreInteractions(players, teams, appearances);
    }

    @Test
    void delete_whenExists_invokesDeletesInOrder_andReturnsMessage() {
        UUID id = UUID.randomUUID();
        given(players.existsById(id)).willReturn(true);

        String msg = svc.delete(id);

        assertEquals("Player " + id + " deleted successfully", msg);

        InOrder inOrder = inOrder(players, appearances);
        inOrder.verify(players).existsById(id);
        inOrder.verify(appearances).deleteByPlayerId(id);
        inOrder.verify(players).deleteById(id);
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(teams);
    }

    @Test
    void delete_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(players.existsById(id)).willReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.delete(id));
        assertTrue(ex.getMessage().contains("Player " + id + " not found"));

        verify(players).existsById(id);
        verifyNoMoreInteractions(players, teams, appearances);
    }
}