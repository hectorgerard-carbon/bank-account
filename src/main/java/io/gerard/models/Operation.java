package io.gerard.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Operation (
        UUID operationId,
        UUID accountId,
        OperationTypes operationType,
        BigDecimal amount,
        BigDecimal newBalance,
        Instant dateTime) {}
