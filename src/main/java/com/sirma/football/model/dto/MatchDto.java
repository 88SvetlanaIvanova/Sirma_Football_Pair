package com.sirma.football.model.dto;
import java.time.LocalDate;
import java.util.UUID;

public record MatchDto(
       UUID id, UUID teamAId, UUID teamBId, LocalDate date, String score, Integer baseDuration
) {}