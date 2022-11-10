package io.gerard.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Account(UUID accountId, BigDecimal balance, List<Operation> operations) {
}
