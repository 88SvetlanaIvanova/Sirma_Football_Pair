package com.sirma.football.controller;

import com.sirma.football.model.dto.*;
import com.sirma.football.services.PlayerCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerCrudService svc;

    @GetMapping
    public List<PlayerDto> list(@RequestParam(required = false) UUID teamId) {
        return svc.list(teamId);
    }

    @GetMapping("/{id}")
    public PlayerDto get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @GetMapping("/by-team")
    public List<PlayerDto> searchByTeam(@RequestParam String name) {
        return svc.getPlayersByTeamName(name);
    }

    @PostMapping
    public ResponseEntity<PlayerDto> create(@Valid @RequestBody PlayerCreateDto dto,
                                            @RequestParam(required = false) UUID id) {
        PlayerDto created = svc.create(dto, id);
        return ResponseEntity.created(URI.create("/api/players/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public PlayerDto update(@PathVariable UUID id, @Valid @RequestBody PlayerCreateDto dto) {
        return svc.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        svc.delete(id);
        return ResponseEntity.ok("Player " + id + " deleted successfully");
    }
}