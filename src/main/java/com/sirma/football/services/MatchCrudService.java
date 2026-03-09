package com.sirma.football.services;

import com.sirma.football.model.dto.*;
import com.sirma.football.mappers.CrudMappers;
import com.sirma.football.entities.*;
import com.sirma.football.repositories.*;
import com.sirma.football.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.util.List;

@Service @RequiredArgsConstructor
public class MatchCrudService {
    private final MatchRepository matches;
    private final TeamRepository teams;
    private final AppearanceRepository appearances;

    @Transactional
    public MatchDto create(MatchCreateDto dto, UUID idIfClientSets) {
        Team a = teams.findById(dto.teamAId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamAId() + " not found"));
        Team b = teams.findById(dto.teamBId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamBId() + " not found"));
        Match m = new Match();
        if (idIfClientSets != null) m.setId(idIfClientSets);
        m.setTeamA(a);
        m.setTeamB(b);
        m.setDate(dto.date());
        m.setScore(dto.score());
        m.setBaseDuration(dto.baseDuration());
        return CrudMappers.toDto(matches.save(m));
    }

    @Transactional(readOnly = true)
    public MatchDto get(UUID id) {
        Match m = matches.findById(id).orElseThrow(() -> new NotFoundException("Match " + id + " not found"));
        return CrudMappers.toDto(m);
    }

    @Transactional(readOnly = true)
    public List<MatchDto> list(UUID teamId) {
        List<Match> result = (teamId == null)
                ? matches.findAll()
                : matches.findByTeamA_IdOrTeamB_Id(teamId, teamId);
        return result.stream().map(CrudMappers::toDto).toList();
    }

    @Transactional
    public MatchDto update(UUID id, MatchCreateDto dto) {
        Match m = matches.findById(id).orElseThrow(() -> new NotFoundException("Match " + id + " not found"));
        Team a = teams.findById(dto.teamAId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamAId() + " not found"));
        Team b = teams.findById(dto.teamBId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamBId() + " not found"));
        m.setTeamA(a);
        m.setTeamB(b);
        m.setDate(dto.date());
        m.setScore(dto.score());
        m.setBaseDuration(dto.baseDuration());
        return CrudMappers.toDto(m);
    }

    @Transactional
    public void delete(UUID id) {
        if (!matches.existsById(id)) throw new NotFoundException("Match " + id + " not found");
        appearances.deleteByMatchId(id);
        matches.deleteById(id);
    }
}