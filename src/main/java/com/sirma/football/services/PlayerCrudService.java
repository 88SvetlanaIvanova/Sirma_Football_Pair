package com.sirma.football.services;

import com.sirma.football.mappers.CrudMappers;
import com.sirma.football.entities.*;
import com.sirma.football.model.dto.PlayerCreateDto;
import com.sirma.football.model.dto.PlayerDto;
import com.sirma.football.repositories.*;
import com.sirma.football.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;

@Service @RequiredArgsConstructor
public class PlayerCrudService {
    private final PlayerRepository players;
    private final TeamRepository teams;
    private final AppearanceRepository appearances;

    @Transactional
    public PlayerDto create(PlayerCreateDto dto, UUID idIfClientSets) {
        Team team = teams.findById(dto.teamId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamId() + " not found"));

        Player p = new Player();
        if (idIfClientSets != null) p.setId(idIfClientSets);
        p.setTeam(team);
        p.setTeamNumber(dto.teamNumber());
        p.setPosition(dto.position());
        p.setFullName(dto.fullName());
        return CrudMappers.toDto(players.save(p));
    }

    @Transactional(readOnly = true)
    public PlayerDto get(UUID id) {
        Player p = players.findById(id).orElseThrow(() -> new NotFoundException("Player " + id + " not found"));
        return CrudMappers.toDto(p);
    }

    @Transactional(readOnly = true)
    public List<PlayerDto> list(UUID teamId) {
        List<Player> result = (teamId == null) ? players.findAll() : players.findByTeamId(teamId);
        return result.stream().map(CrudMappers::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerDto> getPlayersByTeamName(String teamName) {
        return players.findByTeam_Name(teamName).stream()
                .map(CrudMappers::toDto)
                .toList();
    }

    @Transactional
    public PlayerDto update(UUID id, PlayerCreateDto dto) {
        Player p = players.findById(id).orElseThrow(() -> new NotFoundException("Player " + id + " not found"));
        Team team = teams.findById(dto.teamId())
                .orElseThrow(() -> new NotFoundException("Team " + dto.teamId() + " not found"));
        p.setTeam(team);
        p.setTeamNumber(dto.teamNumber());
        p.setPosition(dto.position());
        p.setFullName(dto.fullName());
        return CrudMappers.toDto(p);
    }

    @Transactional
    public String delete(UUID id) {
        if (!players.existsById(id)) throw new NotFoundException("Player " + id + " not found");
        appearances.deleteByPlayerId(id);
        players.deleteById(id);
        return "Player " + id + " deleted successfully";
    }
}