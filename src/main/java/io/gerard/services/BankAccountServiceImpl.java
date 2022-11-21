package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.NotEnoughFundsException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Account;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

public class BankAccountServiceImpl implements BankAccountService {
    private final OperationRepository operationRepository;
    private final AccountStatementFormatter accountStatementFormatter;
    private final StringPrinter stringPrinter;
    private final Clock clock;
    private final Supplier<UUID> uuidGenerator;

    BankAccountServiceImpl(OperationRepository operationRepository, AccountStatementFormatter accountStatementFormatter,
                           StringPrinter stringPrinter, Clock clock, Supplier<UUID> uuidGenerator) {
        this.operationRepository = operationRepository;
        this.accountStatementFormatter = accountStatementFormatter;
        this.stringPrinter = stringPrinter;
        this.clock = clock;
        this.uuidGenerator = uuidGenerator;
    }

    public BankAccountServiceImpl(OperationRepository operationRepository,
                                  AccountStatementFormatter accountStatementFormatter, StringPrinter stringPrinter) {
        this(operationRepository, accountStatementFormatter, stringPrinter, Clock.systemDefaultZone(), UUID::randomUUID);
    }

    @Override
    public Operation deposit(UUID accountId, BigDecimal amount)
            throws ZeroOrNegativeAmountException, AccountNotFoundException {

        if (amount.signum() <= 0) {
            throw new ZeroOrNegativeAmountException();
        }
        final var scaledAmount = amount.setScale(2, RoundingMode.HALF_DOWN);
        final var date = Instant.now(clock);

        final var lastBalance = getCurrentBalance(accountId);

        final var newBalance = lastBalance.add(scaledAmount);
        final var operationId = uuidGenerator.get();

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
        final var date = Instant.now(clock);

        final var lastBalance = getCurrentBalance(accountId);
        if (lastBalance.compareTo(amount) < 0) {
            throw new NotEnoughFundsException();
        }
        final var newBalance = lastBalance.subtract(scaledAmount);
        final var operationId = uuidGenerator.get();

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

    @Override
    public void printAccountStatement(UUID accountId) {
        final var operations = operationRepository.getAllOrderByDateDesc(accountId);

        final var mostRecentOperation = operations.stream().findFirst();

        final var actualBalance = mostRecentOperation
                .map(Operation::newBalance)
                .orElse(BigDecimal.ZERO);

        final var statement = accountStatementFormatter.format(new Account(accountId, actualBalance, operations));

        stringPrinter.print(statement);
    }

    private BigDecimal getCurrentBalance(UUID accountId) throws AccountNotFoundException {
        final var optionalLastOperation = operationRepository.getLastOperation(accountId);
        if (optionalLastOperation.isEmpty()) {
            throw new AccountNotFoundException();
        }
        return optionalLastOperation.get().newBalance();
    }
}
