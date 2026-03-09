package com.sirma.football.model.dto;
import java.util.UUID;

public record TeamDto(UUID id, String name, String managerFullName, String groupName) {}