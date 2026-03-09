package com.sirma.football.servicestests;

import com.sirma.football.entities.Team;
import com.sirma.football.model.dto.TeamCreateDto;
import com.sirma.football.model.dto.TeamDto;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.MatchRepository;
import com.sirma.football.repositories.PlayerRepository;
import com.sirma.football.repositories.TeamRepository;
import com.sirma.football.services.TeamCrudService;
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
class TeamCrudServiceTest {

    @Mock TeamRepository teams;
    @Mock PlayerRepository players;
    @Mock AppearanceRepository appearances;
    @Mock MatchRepository matches;

    @InjectMocks TeamCrudService svc;

    private static Team mkTeam(UUID id, String name, String manager, String group) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setManagerFullName(manager);
        t.setGroupName(group);
        return t;
    }

    @Test
    void create_setsProvidedId_andPersists() {
        UUID providedId = UUID.randomUUID();
        TeamCreateDto dto = new TeamCreateDto("Spain", "Luis de la Fuente", "A");

        given(teams.save(any(Team.class))).willAnswer(inv -> inv.getArgument(0));

        TeamDto out = svc.create(dto, providedId);

        assertNotNull(out);
        assertEquals(providedId, out.id());
        assertEquals("Spain", out.name());
        assertEquals("Luis de la Fuente", out.managerFullName());
        assertEquals("A", out.groupName());

        verify(teams).save(any(Team.class));
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void create_generatesIdWhenClientDoesNotProvide() {
        TeamCreateDto dto = new TeamCreateDto("France", "Didier Deschamps", "B");

        given(teams.save(any(Team.class))).willAnswer(inv -> {
            Team t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        TeamDto out = svc.create(dto, null);

        assertNotNull(out);
        assertNotNull(out.id());
        assertEquals("France", out.name());
        assertEquals("Didier Deschamps", out.managerFullName());
        assertEquals("B", out.groupName());

        verify(teams).save(any(Team.class));
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void get_returnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        Team entity = mkTeam(id, "Spain", "Luis de la Fuente", "A");

        given(teams.findById(id)).willReturn(Optional.of(entity));

        TeamDto out = svc.get(id);

        assertEquals(id, out.id());
        assertEquals("Spain", out.name());
        assertEquals("Luis de la Fuente", out.managerFullName());
        assertEquals("A", out.groupName());

        verify(teams).findById(id);
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void get_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(teams.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.get(id));
        assertTrue(ex.getMessage().contains("Team " + id + " not found"));

        verify(teams).findById(id);
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }


    @Test
    void list_mapsAllTeamsToDtos() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        given(teams.findAll()).willReturn(List.of(
                mkTeam(id1, "Spain", "Luis de la Fuente", "A"),
                mkTeam(id2, "France", "Didier Deschamps", "B")
        ));

        List<TeamDto> out = svc.list();

        assertEquals(2, out.size());
        assertEquals(id1, out.get(0).id());
        assertEquals("Spain", out.get(0).name());
        assertEquals(id2, out.get(1).id());
        assertEquals("France", out.get(1).name());

        verify(teams).findAll();
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void update_modifiesManagedEntity_andReturnsDto() {
        UUID id = UUID.randomUUID();
        Team existing = mkTeam(id, "OLD", "Old Manager", "Z");

        TeamCreateDto dto = new TeamCreateDto("Italy", "Luciano Spalletti", "C");

        given(teams.findById(id)).willReturn(Optional.of(existing));

        TeamDto out = svc.update(id, dto);

        assertEquals(id, out.id());
        assertEquals("Italy", out.name());
        assertEquals("Luciano Spalletti", out.managerFullName());
        assertEquals("C", out.groupName());

        verify(teams).findById(id);
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void update_throws_whenTeamNotFound() {
        UUID id = UUID.randomUUID();
        TeamCreateDto dto = new TeamCreateDto("Portugal", "Roberto Martinez", "F");

        given(teams.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(id, dto));
        assertTrue(ex.getMessage().contains("Team " + id + " not found"));

        verify(teams).findById(id);
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void delete_whenExists_invokesDeletesInRequiredOrder() {
        UUID id = UUID.randomUUID();
        given(teams.existsById(id)).willReturn(true);

        svc.delete(id);

        InOrder in = inOrder(teams, appearances, matches, players);
        in.verify(teams).existsById(id);
        in.verify(appearances).deleteByTeamId(id);
        in.verify(appearances).deleteByMatchesOfTeam(id);
        in.verify(matches).deleteByTeamId(id);
        in.verify(players).deleteByTeamId(id);
        in.verify(teams).deleteById(id);
        in.verifyNoMoreInteractions();

        verifyNoMoreInteractions(teams, players, appearances, matches);
    }

    @Test
    void delete_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(teams.existsById(id)).willReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.delete(id));
        assertTrue(ex.getMessage().contains("Team " + id + " not found"));

        verify(teams).existsById(id);
        verifyNoMoreInteractions(teams, players, appearances, matches);
    }
}