package com.sirma.football.servicestests;

import com.sirma.football.entities.Appearance;
import com.sirma.football.entities.Match;
import com.sirma.football.entities.Player;
import com.sirma.football.entities.Team;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.MatchRepository;
import com.sirma.football.repositories.PlayerRepository;
import com.sirma.football.repositories.TeamRepository;
import com.sirma.football.services.CsvImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock TeamRepository teamRepo;
    @Mock PlayerRepository playerRepo;
    @Mock MatchRepository matchRepo;
    @Mock AppearanceRepository appRepo;

    @InjectMocks CsvImportService svc;

    private static Reader readerOf(String... lines) {
        return new StringReader(String.join("\n", lines));
    }

    private static <T> List<T> toList(Iterable<T> it) {
        return (it instanceof Collection<T> c)
                ? new ArrayList<>(c)
                : StreamSupport.stream(it.spliterator(), false).toList();
    }


    @Test
    void importTeams_skipsHeader_andImportsOnlyNew_on_saveAll() {

        Reader csv = readerOf(
                "id,name,managerFullName,groupName",
                "1,Spain,Luis de la Fuente,A",
                "2,France,Didier Deschamps,B"
        );

        given(teamRepo.findByLegacyId(1L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(2L)).willReturn(Optional.of(new Team()));

        var report = svc.importTeams(csv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Team>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(teamRepo, times(1)).saveAll(captor.capture());
        List<Team> saved = toList(captor.getValue());

        assertEquals(1, saved.size(), "Only the non-duplicate team should be saved");
        Team t = saved.get(0);
        assertEquals(1L, t.getLegacyId());
        assertEquals("Spain", t.getName());
        assertEquals("Luis de la Fuente", t.getManagerFullName());
        assertEquals("A", t.getGroupName());

        verify(teamRepo).findByLegacyId(1L);
        verify(teamRepo).findByLegacyId(2L);
        verifyNoMoreInteractions(teamRepo);

        assertNotNull(report);
    }

    @Test
    void importTeams_ignoresBlankAndCommentLines_andHandlesNoHeader() {
        Reader csv = readerOf(
                "",             // blank
                "# comment",    // comment
                "3,Italy,Luciano Spalletti,C", // first actual row
                "   ",          // blank
                "4,Portugal,Roberto Martinez,F"
        );

        given(teamRepo.findByLegacyId(3L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(4L)).willReturn(Optional.empty());

        svc.importTeams(csv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Team>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(teamRepo).saveAll(captor.capture());
        List<Team> saved = toList(captor.getValue());

        assertEquals(2, saved.size());
        assertEquals(3L, saved.get(0).getLegacyId());
        assertEquals(4L, saved.get(1).getLegacyId());
    }

    @Test
    void importPlayers_happyPath_savesPlayerWithResolvedTeam() {

        Reader csv = readerOf(
                "id,teamNumber,position,fullName,teamId",
                "10,9,FW,Fernando Torres,1"
        );

        Team team = new Team();
        team.setLegacyId(1L);

        given(playerRepo.findByLegacyId(10L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(1L)).willReturn(Optional.of(team));

        svc.importPlayers(csv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Player>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(playerRepo).saveAll(captor.capture());
        List<Player> saved = toList(captor.getValue());

        assertEquals(1, saved.size());
        Player p = saved.getFirst();
        assertEquals(10L, p.getLegacyId());
        assertEquals(9, p.getTeamNumber());
        assertEquals("FW", p.getPosition());
        assertEquals("Fernando Torres", p.getFullName());
        assertSame(team, p.getTeam());
    }

    @Test
    void importPlayers_skipsRow_whenTeamNotFound_orDuplicatePlayer() {
        Reader csv = readerOf(
                "id,teamNumber,position,fullName,teamId",
                "10,9,FW,Fernando Torres,1",
                "11,8,MF,Andres Iniesta,2"
        );

        given(playerRepo.findByLegacyId(10L)).willReturn(Optional.of(new Player()));
        given(playerRepo.findByLegacyId(11L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(2L)).willReturn(Optional.empty());

        var report = svc.importPlayers(csv);

        verify(playerRepo, never()).saveAll(any());
        verify(playerRepo).findByLegacyId(10L);
        verify(playerRepo).findByLegacyId(11L);
        verify(teamRepo).findByLegacyId(2L);
        verifyNoMoreInteractions(playerRepo, teamRepo);

        assertNotNull(report);
    }

    @Test
    void importMatches_parsesFlexibleDates_andSavesMatches() {
        Reader csv = readerOf(
                "id,a,b,date,score",
                "100,1,2,2024-06-14,2-1",
                "101,1,2,14.06.2024,0-0"
        );

        Team a = new Team(); a.setLegacyId(1L);
        Team b = new Team(); b.setLegacyId(2L);

        given(matchRepo.findByLegacyId(100L)).willReturn(Optional.empty());
        given(matchRepo.findByLegacyId(101L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(1L)).willReturn(Optional.of(a));
        given(teamRepo.findByLegacyId(2L)).willReturn(Optional.of(b));

        svc.importMatches(csv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Match>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(matchRepo).saveAll(captor.capture());
        List<Match> saved = toList(captor.getValue());

        assertEquals(2, saved.size());
        assertEquals(100L, saved.get(0).getLegacyId());
        assertEquals(LocalDate.of(2024, 6, 14), saved.get(0).getDate());
        assertEquals("2-1", saved.get(0).getScore());

        assertEquals(101L, saved.get(1).getLegacyId());
        assertEquals(LocalDate.of(2024, 6, 14), saved.get(1).getDate()); // parsed from 14.06.2024
        assertEquals("0-0", saved.get(1).getScore());
    }

    @Test
    void importMatches_skipsDuplicate_andMissingTeams() {
        Reader csv = readerOf(
                "id,a,b,date,score",
                "100,1,2,2024-06-14,2-1",
                "101,9,2,2024-06-14,1-1"
        );

        given(matchRepo.findByLegacyId(100L)).willReturn(Optional.of(new Match()));
        given(matchRepo.findByLegacyId(101L)).willReturn(Optional.empty());
        given(teamRepo.findByLegacyId(9L)).willReturn(Optional.empty());

        svc.importMatches(csv);

        verify(matchRepo, never()).saveAll(any());
        verify(matchRepo).findByLegacyId(100L);
        verify(matchRepo).findByLegacyId(101L);
        verify(teamRepo).findByLegacyId(9L);
        verifyNoMoreInteractions(matchRepo, teamRepo);
    }

    @Test
    void importAppearances_savesOnlyValidRows() {

        Reader csv = readerOf(
                "id,playerId,matchId,fromMinutes,toMinutes",
                "1,10,100,0,45",   // valid
                "2,10,100,50,40"   // invalid: to <= from -> skip
        );

        Player player = new Player(); player.setLegacyId(10L);
        Match match = new Match(); match.setLegacyId(100L);

        given(playerRepo.findByLegacyId(10L)).willReturn(Optional.of(player));
        given(matchRepo.findByLegacyId(100L)).willReturn(Optional.of(match));

        svc.importAppearances(csv);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Appearance>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(appRepo).saveAll(captor.capture());
        List<Appearance> saved = toList(captor.getValue());

        assertEquals(1, saved.size());
        Appearance ap = saved.get(0);
        assertEquals(1L, ap.getLegacyId());
        assertSame(player, ap.getPlayer());
        assertSame(match, ap.getMatch());
        assertEquals(0, ap.getFromMinute());
        assertEquals(45, ap.getToMinute());
    }

    @Test
    void importAppearances_skipsWhenRefsMissing() {
        Reader csv = readerOf(
                "id,playerId,matchId,fromMinutes,toMinutes",
                "3,99,100,0,10",
                "4,10,98,0,10"
        );

        given(playerRepo.findByLegacyId(99L)).willReturn(Optional.empty());
        given(playerRepo.findByLegacyId(10L)).willReturn(Optional.of(new Player()));
        given(matchRepo.findByLegacyId(98L)).willReturn(Optional.empty());

        svc.importAppearances(csv);

        verify(appRepo, never()).saveAll(any());

        verify(appRepo, times(2)).existsById(any());

        verify(playerRepo).findByLegacyId(99L);
        verify(playerRepo).findByLegacyId(10L);
        verify(matchRepo).findByLegacyId(98L);

        verifyNoMoreInteractions(playerRepo, matchRepo, appRepo);
    }
}