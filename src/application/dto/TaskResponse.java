package application.dto;

import java.time.Instant;

public record TaskResponse(Long id, String type, String status, String payload) {
}
