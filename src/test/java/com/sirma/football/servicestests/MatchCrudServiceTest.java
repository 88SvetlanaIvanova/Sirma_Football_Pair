package com.sirma.football.servicestests;

import com.sirma.football.entities.Match;
import com.sirma.football.entities.Team;
import com.sirma.football.model.dto.MatchCreateDto;
import com.sirma.football.model.dto.MatchDto;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.MatchRepository;
import com.sirma.football.repositories.TeamRepository;
import com.sirma.football.services.MatchCrudService;
import com.sirma.football.web.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCrudServiceTest {

    @Mock MatchRepository matches;
    @Mock TeamRepository teams;
    @Mock AppearanceRepository appearances;

    @InjectMocks MatchCrudService svc;

    private static Team mkTeam(UUID id) {
        Team t = new Team();
        t.setId(id);
        return t;
    }

    private static Match mkMatch(UUID id, Team a, Team b, LocalDate date, String score, Integer baseDuration) {
        Match m = new Match();
        m.setId(id);
        m.setTeamA(a);
        m.setTeamB(b);
        m.setDate(date);
        m.setScore(score);
        m.setBaseDuration(baseDuration);
        return m;
    }

    @Test
    void create_setsProvidedId_andPersists() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        UUID clientProvidedId = UUID.randomUUID();

        Team a = mkTeam(aId);
        Team b = mkTeam(bId);

        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.of(2024, 6, 14), "2-1", 95);

        given(teams.findById(aId)).willReturn(Optional.of(a));
        given(teams.findById(bId)).willReturn(Optional.of(b));

        Answer<Match> saveAnswer = inv -> {
            return inv.getArgument(0);
        };
        given(matches.save(any(Match.class))).willAnswer(saveAnswer);

        MatchDto out = svc.create(dto, clientProvidedId);

        assertNotNull(out);
        assertEquals(clientProvidedId, out.id());
        assertEquals(aId, out.teamAId());
        assertEquals(bId, out.teamBId());
        assertEquals(LocalDate.of(2024, 6, 14), out.date());
        assertEquals("2-1", out.score());
        assertEquals(95, out.baseDuration());

        verify(teams).findById(aId);
        verify(teams).findById(bId);
        verify(matches).save(any(Match.class));
        verifyNoMoreInteractions(teams, matches, appearances);
    }

    @Test
    void create_generatesIdWhenClientDoesNotProvide() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        Team a = mkTeam(aId);
        Team b = mkTeam(bId);

        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.of(2024, 6, 14), "1-0", 90);

        given(teams.findById(aId)).willReturn(Optional.of(a));
        given(teams.findById(bId)).willReturn(Optional.of(b));

        given(matches.save(any(Match.class))).willAnswer(inv -> {
            Match m = inv.getArgument(0);
            if (m.getId() == null) m.setId(UUID.randomUUID());
            return m;
        });

        MatchDto out = svc.create(dto, null);

        assertNotNull(out);
        assertNotNull(out.id());
        assertEquals(aId, out.teamAId());
        assertEquals(bId, out.teamBId());
        assertEquals("1-0", out.score());
        assertEquals(90, out.baseDuration());
    }

    @Test
    void create_throws_whenTeamA_NotFound() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        given(teams.findById(aId)).willReturn(Optional.empty());

        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.now(), "0-0", 90);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.create(dto, null));
        assertTrue(ex.getMessage().contains("Team " + aId + " not found"));
        verify(teams).findById(aId);
        verifyNoMoreInteractions(teams, matches, appearances);
    }

    @Test
    void create_throws_whenTeamB_NotFound() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        given(teams.findById(aId)).willReturn(Optional.of(mkTeam(aId)));
        given(teams.findById(bId)).willReturn(Optional.empty());

        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.now(), "0-0", 90);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.create(dto, null));
        assertTrue(ex.getMessage().contains("Team " + bId + " not found"));
        verify(teams).findById(aId);
        verify(teams).findById(bId);
        verifyNoMoreInteractions(teams, matches, appearances);
    }


    @Test
    void get_returnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        Team a = mkTeam(aId);
        Team b = mkTeam(bId);
        Match m = mkMatch(id, a, b, LocalDate.of(2024, 6, 14), "3-2", 90);

        given(matches.findById(id)).willReturn(Optional.of(m));

        MatchDto out = svc.get(id);

        assertEquals(id, out.id());
        assertEquals(aId, out.teamAId());
        assertEquals(bId, out.teamBId());
        assertEquals("3-2", out.score());
        assertEquals(90, out.baseDuration());

        verify(matches).findById(id);
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void get_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(matches.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.get(id));
        assertTrue(ex.getMessage().contains("Match " + id + " not found"));

        verify(matches).findById(id);
        verifyNoMoreInteractions(matches, teams, appearances);
    }


    @Test
    void list_all_whenTeamIdNull() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        Team a = mkTeam(aId);
        Team b = mkTeam(bId);

        given(matches.findAll()).willReturn(List.of(
                mkMatch(id1, a, b, LocalDate.of(2024, 6, 14), "1-1", 90),
                mkMatch(id2, b, a, LocalDate.of(2024, 6, 15), "0-2", 90)
        ));

        List<MatchDto> out = svc.list(null);

        assertEquals(2, out.size());
        assertEquals(id1, out.get(0).id());
        assertEquals(id2, out.get(1).id());
        verify(matches).findAll();
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void list_byTeam_whenTeamIdProvided() {
        UUID teamId = UUID.randomUUID();
        UUID otherTeam = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Team t = mkTeam(teamId);
        Team o = mkTeam(otherTeam);

        given(matches.findByTeamA_IdOrTeamB_Id(teamId, teamId))
                .willReturn(List.of(mkMatch(id, t, o, LocalDate.of(2024, 6, 14), "2-0", 90)));

        List<MatchDto> out = svc.list(teamId);

        assertEquals(1, out.size());
        assertEquals(id, out.get(0).id());
        assertEquals(teamId, out.get(0).teamAId()); // could be A or B; here we set as A
        verify(matches).findByTeamA_IdOrTeamB_Id(teamId, teamId);
        verifyNoMoreInteractions(matches, teams, appearances);
    }


    @Test
    void update_modifiesManagedEntity_andReturnsDto() {
        UUID id = UUID.randomUUID();
        UUID oldA = UUID.randomUUID();
        UUID oldB = UUID.randomUUID();
        UUID newA = UUID.randomUUID();
        UUID newB = UUID.randomUUID();

        Team oldATeam = mkTeam(oldA);
        Team oldBTeam = mkTeam(oldB);
        Match m = mkMatch(id, oldATeam, oldBTeam, LocalDate.of(2024, 5, 1), "0-0", 90);

        Team aNew = mkTeam(newA);
        Team bNew = mkTeam(newB);

        MatchCreateDto dto = new MatchCreateDto(newA, newB, LocalDate.of(2024, 6, 20), "4-3", 95);

        given(matches.findById(id)).willReturn(Optional.of(m));
        given(teams.findById(newA)).willReturn(Optional.of(aNew));
        given(teams.findById(newB)).willReturn(Optional.of(bNew));

        MatchDto out = svc.update(id, dto);

        assertEquals(id, out.id());
        assertEquals(newA, out.teamAId());
        assertEquals(newB, out.teamBId());
        assertEquals(LocalDate.of(2024, 6, 20), out.date());
        assertEquals("4-3", out.score());
        assertEquals(95, out.baseDuration());

        verify(matches).findById(id);
        verify(teams).findById(newA);
        verify(teams).findById(newB);
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void update_throws_whenMatchNotFound() {
        UUID id = UUID.randomUUID();
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.now(), "1-1", 90);

        given(matches.findById(id)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(id, dto));
        assertTrue(ex.getMessage().contains("Match " + id + " not found"));

        verify(matches).findById(id);
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void update_throws_whenTeamLookupFails() {
        UUID id = UUID.randomUUID();
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        MatchCreateDto dto = new MatchCreateDto(aId, bId, LocalDate.now(), "1-1", 90);
        given(matches.findById(id)).willReturn(Optional.of(mkMatch(id, mkTeam(UUID.randomUUID()), mkTeam(UUID.randomUUID()), LocalDate.now(), "x", 90)));
        given(teams.findById(aId)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(id, dto));
        assertTrue(ex.getMessage().contains("Team " + aId + " not found"));

        verify(matches).findById(id);
        verify(teams).findById(aId);
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void delete_whenExists_invokesDeleteInOrder() {
        UUID id = UUID.randomUUID();
        given(matches.existsById(id)).willReturn(true);

        svc.delete(id);

        verify(matches).existsById(id);
        verify(appearances).deleteByMatchId(id);
        verify(matches).deleteById(id);
        verifyNoMoreInteractions(matches, teams, appearances);
    }

    @Test
    void delete_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(matches.existsById(id)).willReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.delete(id));
        assertTrue(ex.getMessage().contains("Match " + id + " not found"));

        verify(matches).existsById(id);
        verifyNoMoreInteractions(matches, teams, appearances);
    }
}
