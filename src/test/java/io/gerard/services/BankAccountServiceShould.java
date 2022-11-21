package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.NotEnoughFundsException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Account;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceShould {
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private AccountStatementFormatter accountStatementFormatter;

    @Mock
    private Supplier<UUID> uuidGenerator;

    @Mock
    private StringPrinter stringPrinter;

    private final Instant instant = Instant.parse("2022-11-10T12:35:24.00Z");

    @BeforeEach
    void setUp() {
        final var clock = Clock.fixed(instant, ZoneId.of("Europe/Paris"));
        bankAccountService = new BankAccountServiceImpl(
                operationRepository,
                accountStatementFormatter,
                stringPrinter,
                clock,
                uuidGenerator
        );
    }

    @Test
    void makeADepositWithExistingAccountAndPositiveAmount()
            throws ZeroOrNegativeAmountException, AccountNotFoundException {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var operationId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae4");
        final var amount = BigDecimal.TEN.setScale(2, RoundingMode.HALF_DOWN);

        final var lastOperation = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(100),
                instant
        );

        final var expected = new Operation(
                operationId,
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(110.00).setScale(2, RoundingMode.HALF_DOWN),
                instant
        );

        when(uuidGenerator.get()).thenReturn(operationId);
        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));
        when(operationRepository.add(expected)).thenReturn(expected);

        final var actual = bankAccountService.deposit(accountId, amount);

        assertEquals(expected, actual);

        verify(uuidGenerator).get();
        verify(operationRepository).getLastOperation(accountId);
        verify(operationRepository).add(expected);
        verifyNoMoreInteractions(uuidGenerator, operationRepository);
        verifyNoInteractions(accountStatementFormatter, stringPrinter);
    }

    @Test
    void throwNotFoundExceptionWhenDepositWithNonExistingAccountAndPositiveAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN.setScale(2, RoundingMode.HALF_DOWN);


        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> bankAccountService.deposit(accountId, amount));

        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);
        verifyNoInteractions(uuidGenerator, accountStatementFormatter, stringPrinter);
    }

    @Test
    void throwNegAmountExceptionWhenDepositWithExistingAccountAndNegativeAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.valueOf(-10);

        assertThrows(ZeroOrNegativeAmountException.class, () -> bankAccountService.deposit(accountId, amount));

        verifyNoInteractions(operationRepository, uuidGenerator, accountStatementFormatter, stringPrinter);
    }

    @Test
    void makeAWithdrawWithExistingAccountAndPositiveAmountAndEnoughFunds()
            throws ZeroOrNegativeAmountException, AccountNotFoundException, NotEnoughFundsException {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var operationId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae4");
        final var amount = BigDecimal.TEN.setScale(2, RoundingMode.HALF_DOWN);

        final var lastOperation = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(110),
                instant
        );

        final var expected = new Operation(
                operationId,
                accountId,
                OperationTypes.WITHDRAWAL,
                amount,
                BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_DOWN),
                instant
        );

        when(uuidGenerator.get()).thenReturn(operationId);
        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));
        when(operationRepository.add(expected)).thenReturn(expected);

        final var actual = bankAccountService.withdraw(accountId, amount);

        assertEquals(expected, actual);

        verify(uuidGenerator).get();
        verify(operationRepository).getLastOperation(accountId);
        verify(operationRepository).add(expected);
        verifyNoMoreInteractions(uuidGenerator, operationRepository);
        verifyNoInteractions(accountStatementFormatter, stringPrinter);
    }

    @Test
    void throwNotFoundExceptionWhenWithdrawWithNonExistingAccountAndPositiveAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;


        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> bankAccountService.withdraw(accountId, amount));

        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);
        verifyNoInteractions(uuidGenerator, accountStatementFormatter, stringPrinter);
    }

    @Test
    void throwNegAmountExceptionWhenWithdrawWithExistingAccountAndNegativeAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.valueOf(-10);

        assertThrows(ZeroOrNegativeAmountException.class, () -> bankAccountService.withdraw(accountId, amount));

        verifyNoInteractions(uuidGenerator, operationRepository, accountStatementFormatter, stringPrinter);
    }

    @Test
    void throwNotEnoughFundsExceptionWhenWithdrawWithExistingAccountAndAmountGreaterThanBalance() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;

        final var lastOperation = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(5),
                instant
        );

        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));

        assertThrows(NotEnoughFundsException.class, () -> bankAccountService.withdraw(accountId, amount));

        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);
        verifyNoInteractions(uuidGenerator, accountStatementFormatter, stringPrinter);
    }

    @Test
    void printStatementWithOperations() {

        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var balance = BigDecimal.valueOf(3320.0);

        final var dateTime1 = LocalDateTime.parse("2022-10-02T09:59:59").toInstant(ZoneOffset.UTC);
        final var dateTime2 = LocalDateTime.parse("2022-10-06T17:10:31").toInstant(ZoneOffset.UTC);
        final var dateTime3 = LocalDateTime.parse("2022-10-15T12:05").toInstant(ZoneOffset.UTC);
        final var dateTime4 = LocalDateTime.parse("2022-10-29T21:30:18").toInstant(ZoneOffset.UTC);

        final var op1 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(2100.0), dateTime1);
        final var op2 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(2150.0), dateTime2);
        final var op3 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(1200.0),
                BigDecimal.valueOf(3350.0), dateTime3);
        final var op4 = new Operation(UUID.randomUUID(), accountId, OperationTypes.WITHDRAWAL, BigDecimal.valueOf(30.0),
                BigDecimal.valueOf(3320.0), dateTime4);

        final var operationList = List.of(op4, op3, op2, op1);

        final var expected = """
                Your account : f910cf03-e534-4d9d-a473-94ebe3d2cae3
                	your last balance is : 3320.0
                Operations :\s
                Date 					Type 		Amount\s
                2022-10-29T21:30:18		WITHDRAW	30.0
                2022-10-15T12:05   		DEPOSIT		1200.0
                2022-10-06T17:10:31		DEPOSIT		50.0
                2022-10-02T09:59:59		DEPOSIT		100.0""";

        final var account = new Account(accountId, balance, operationList);

        when(operationRepository.getAllOrderByDateDesc(accountId)).thenReturn(operationList);
        when(accountStatementFormatter.format(account))
                .thenReturn(expected);
        doNothing().when(stringPrinter).print(expected);

        assertDoesNotThrow(() ->bankAccountService.printAccountStatement(accountId));

        verify(operationRepository).getAllOrderByDateDesc(accountId);
        verify(accountStatementFormatter).format(account);
        verify(stringPrinter).print(expected);

        verifyNoMoreInteractions(operationRepository, accountStatementFormatter, stringPrinter);
        verifyNoInteractions(uuidGenerator);
    }
}
