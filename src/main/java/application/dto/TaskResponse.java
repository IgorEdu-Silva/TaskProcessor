package application.dto;

import java.util.UUID;

public record TaskResponse(UUID id, String type, String status, String payload) {
}
