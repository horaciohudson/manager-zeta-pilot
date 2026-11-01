package com.zetapilot.manager.dto;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserResponse(UUID id, String email, String fullName, List<String> roles) {
}