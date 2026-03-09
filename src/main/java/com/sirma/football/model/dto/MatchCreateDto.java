package com.sirma.football.model.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record MatchCreateDto(
        @NotNull UUID teamAId,
        @NotNull UUID teamBId,
        @PastOrPresent
        @NotNull LocalDate date,
        @NotBlank String score,
        @NotNull @Min(90) @Max(150) Integer baseDuration


) {
    @AssertTrue(message = "Teams must be different")
    public boolean isValidTeams() {
        return teamAId != null && teamBId != null && !teamAId.equals(teamBId);
    }
}
