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
public class TeamCrudService {
    private final TeamRepository teams;
    private final PlayerRepository players;
    private final AppearanceRepository appearances;
    private final MatchRepository matches;

    @Transactional
    public TeamDto create(TeamCreateDto dto, UUID idIfClientSets) {
        Team t = new Team();
        if (idIfClientSets != null) t.setId(idIfClientSets);
        t.setName(dto.name());
        t.setManagerFullName(dto.managerFullName());
        t.setGroupName(dto.groupName());
        return CrudMappers.toDto(teams.save(t));
    }

    @Transactional(readOnly = true)
    public TeamDto get(UUID id) {
        Team t = teams.findById(id).orElseThrow(() -> new NotFoundException("Team " + id + " not found"));
        return CrudMappers.toDto(t);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> list() {
        return teams.findAll().stream().map(CrudMappers::toDto).toList();
    }

    @Transactional
    public TeamDto update(UUID id, TeamCreateDto dto) {
        Team t = teams.findById(id).orElseThrow(() -> new NotFoundException("Team " + id + " not found"));
        t.setName(dto.name());
        t.setManagerFullName(dto.managerFullName());
        t.setGroupName(dto.groupName());
        return CrudMappers.toDto(t);
    }

    @Transactional
    public void delete(UUID id) {
        if (!teams.existsById(id)) throw new NotFoundException("Team " + id + " not found");
        appearances.deleteByTeamId(id);
        appearances.deleteByMatchesOfTeam(id);
        matches.deleteByTeamId(id);
        players.deleteByTeamId(id);
        teams.deleteById(id);
    }
}