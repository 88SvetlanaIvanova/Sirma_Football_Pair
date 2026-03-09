package com.sirma.football.controller;

import com.sirma.football.model.dto.*;
import com.sirma.football.services.MatchCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchCrudService svc;

    @GetMapping
    public List<MatchDto> list(@RequestParam(required = false) UUID teamId) {
        return svc.list(teamId);
    }

    @GetMapping("/{id}")
    public MatchDto get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @PostMapping
    public ResponseEntity<MatchDto> create(@Valid @RequestBody MatchCreateDto dto,
                                           @RequestParam(required = false) UUID id) {
        MatchDto created = svc.create(dto, id);
        return ResponseEntity.created(URI.create("/api/matches/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MatchDto update(@PathVariable UUID id, @Valid @RequestBody MatchCreateDto dto) {
        return svc.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        svc.delete(id);
        return ResponseEntity.ok("Match " + id + " deleted successfully");
    }
}