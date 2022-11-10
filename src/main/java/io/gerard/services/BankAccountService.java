package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Operation;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankAccountService {
    Operation deposit(UUID accountId, BigDecimal amount) throws ZeroOrNegativeAmountException, AccountNotFoundException;
}
