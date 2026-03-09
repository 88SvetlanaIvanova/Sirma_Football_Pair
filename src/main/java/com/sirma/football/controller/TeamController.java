package com.sirma.football.controller;

import com.sirma.football.model.dto.*;
import com.sirma.football.services.TeamCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamCrudService svc;

    @GetMapping
    public List<TeamDto> list() {
        return svc.list();
    }

    @GetMapping("/{id}")
    public TeamDto get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(@Valid @RequestBody TeamCreateDto dto,
                                          @RequestParam(required = false) UUID id) {
        TeamDto created = svc.create(dto, id);
        return ResponseEntity.created(URI.create("/api/teams/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TeamDto update(@PathVariable UUID id, @Valid @RequestBody TeamCreateDto dto) {
        return svc.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        svc.delete(id);
        return ResponseEntity.ok("Team " + id + " deleted successfully");
    }
}