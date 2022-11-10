package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.NotEnoughFundsException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public class BankAccountServiceImpl implements BankAccountService {
    private final OperationRepository operationRepository;

    public BankAccountServiceImpl(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Operation deposit(UUID accountId, BigDecimal amount)
            throws ZeroOrNegativeAmountException, AccountNotFoundException {

        if (amount.signum() <= 0) {
            throw new ZeroOrNegativeAmountException();
        }
        final var scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN);
        final var date = Instant.now();
        final var operationId = UUID.randomUUID();

        final var lastBalance = getCurrentBalance(accountId);

        final var newBalance = lastBalance.add(scaledAmount);

        return operationRepository.add(
                new Operation(
                        operationId,
                        accountId,
                        OperationTypes.DEPOSIT,
                        scaledAmount,
                        newBalance,
                        date
                )
        );
    }

    @Override
    public Operation withdraw(UUID accountId, BigDecimal amount)
            throws ZeroOrNegativeAmountException, AccountNotFoundException, NotEnoughFundsException {

        if (amount.signum() <= 0) {
            throw new ZeroOrNegativeAmountException();
        }
        final var scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN);
        final var date = Instant.now();
        final var operationId = UUID.randomUUID();

        final var lastBalance = getCurrentBalance(accountId);
        if (lastBalance.compareTo(amount) < 0) {
            throw new NotEnoughFundsException();
        }
        final var newBalance = lastBalance.subtract(scaledAmount);

        return operationRepository.add(
                new Operation(
                        operationId,
                        accountId,
                        OperationTypes.WITHDRAWAL,
                        scaledAmount,
                        newBalance,
                        date
                )
        );
    }

    private BigDecimal getCurrentBalance(UUID accountId) throws AccountNotFoundException {
        final var optionalLastOperation = operationRepository.getLastOperation(accountId);
        if (optionalLastOperation.isEmpty()) {
            throw new AccountNotFoundException();
        }
        return optionalLastOperation.get().newBalance();
    }
}
