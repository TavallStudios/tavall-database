package org.tavall.database.postgres;

import java.time.Instant;
import java.util.UUID;

public record PlayerData(UUID playerId, String username, Instant createdAt) {
}
