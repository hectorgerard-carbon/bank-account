package io.gerard.services;

import io.gerard.models.Operation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationRepository {
    Optional<Operation> getLastOperation(UUID accountId);
    Operation add(Operation operation);
    List<Operation> getAllOrderByDateDesc(UUID accountId);
}
