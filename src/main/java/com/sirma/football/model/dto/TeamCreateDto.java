package com.sirma.football.model.dto;

import jakarta.validation.constraints.*;

public record TeamCreateDto(
        @NotBlank @Size(max=50) String name,
        @NotBlank @Size(max=50) String managerFullName,
        @NotBlank @Size(max=5) String groupName
) {}

