package com.sirma.football.servicestests;

import com.sirma.football.repositories.PairAnalysisRepository;
import com.sirma.football.model.util.PairMatchDto;
import com.sirma.football.model.util.PairResultDto;
import com.sirma.football.services.PairAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PairAnalysisServiceTest {

    @Mock
    PairAnalysisRepository repo;

    @InjectMocks
    PairAnalysisService svc;

    @Test
    void top_returnsRepositoryValue() {
        PairResultDto result = new PairResultDto(113L, 128L, 84);
        when(repo.findTopPair()).thenReturn(Optional.of(result));

        Optional<PairResultDto> actual = svc.top();

        assertTrue(actual.isPresent());
        assertEquals(113L, actual.get().p1());
        assertEquals(128L, actual.get().p2());
        assertEquals(84, actual.get().totalMinutes());
        verify(repo).findTopPair();
        verifyNoMoreInteractions(repo);
    }

    @Test
    void top_emptyWhenRepositoryEmpty() {
        when(repo.findTopPair()).thenReturn(Optional.empty());

        Optional<PairResultDto> actual = svc.top();

        assertTrue(actual.isEmpty());
        verify(repo).findTopPair();
        verifyNoMoreInteractions(repo);
    }

    @Test
    void breakdown_delegatesAndReturnsList() {

        List<PairMatchDto> rows = List.of(
                new PairMatchDto(113L, 128L, 101L, 52),
                new PairMatchDto(113L, 128L, 120L, 32)
        );

        when(repo.findByMatchForPair(113L, 128L)).thenReturn(rows);

        List<PairMatchDto> actual = svc.breakdown(113L, 128L);

        assertEquals(2, actual.size());
        assertEquals(101L, actual.get(0).matchId());
        assertEquals(52, actual.get(0).minutesTogether());
        assertEquals(120L, actual.get(1).matchId());
        assertEquals(32, actual.get(1).minutesTogether());

        verify(repo).findByMatchForPair(113L, 128L);
        verifyNoMoreInteractions(repo);
    }
}