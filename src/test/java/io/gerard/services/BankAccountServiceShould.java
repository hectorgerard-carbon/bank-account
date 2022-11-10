package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.NotEnoughFundsException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Account;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceShould {
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private AccountStatementFormatter accountStatementFormatter;

    @Mock
    private StringPrinter stringPrinter;

    @Test
    void makeADepositWithExistingAccountAndPositiveAmount()
            throws ZeroOrNegativeAmountException, AccountNotFoundException {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;

        final var lastOperation = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(100),
                Instant.parse("2022-11-10T12:35:24.00Z")
        );

        final var expected = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(110),
                Instant.now()
        );

        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));
        when(operationRepository.add(any())).thenReturn(expected);

        final var actual = bankAccountService.deposit(accountId, amount);

        assertEquals(expected, actual);
    }

    @Test
    void throwNotFoundExceptionWhenDepositWithNonExistingAccountAndPositiveAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;


        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> bankAccountService.deposit(accountId, amount));
    }

    @Test
    void throwNegAmountExceptionWhenDepositWithExistingAccountAndNegativeAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.valueOf(-10);

        assertThrows(ZeroOrNegativeAmountException.class, () -> bankAccountService.deposit(accountId, amount));
    }

    @Test
    void makeAWithdrawWithExistingAccountAndPositiveAmountAndEnoughFunds()
            throws ZeroOrNegativeAmountException, AccountNotFoundException, NotEnoughFundsException {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;

        final var lastOperation = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.DEPOSIT,
                amount,
                BigDecimal.valueOf(100),
                Instant.parse("2022-11-10T12:35:24.00Z")
        );

        final var expected = new Operation(
                UUID.randomUUID(),
                accountId,
                OperationTypes.WITHDRAWAL,
                amount,
                BigDecimal.valueOf(90),
                Instant.now()
        );

        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));
        when(operationRepository.add(any())).thenReturn(expected);

        final var actual = bankAccountService.withdraw(accountId, amount);

        assertEquals(expected, actual);
    }

    @Test
    void throwNotFoundExceptionWhenWithdrawWithNonExistingAccountAndPositiveAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.TEN;


        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> bankAccountService.withdraw(accountId, amount));
    }

    @Test
    void throwNegAmountExceptionWhenWithdrawWithExistingAccountAndNegativeAmount() {
        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var amount = BigDecimal.valueOf(-10);

        assertThrows(ZeroOrNegativeAmountException.class, () -> bankAccountService.withdraw(accountId, amount));
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
                Instant.parse("2022-11-10T12:35:24.00Z")
        );

        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(lastOperation));

        assertThrows(NotEnoughFundsException.class, () -> bankAccountService.withdraw(accountId, amount));
    }

    @Test
    void printStatementWithOperations() {

        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");
        final var balance = BigDecimal.valueOf(3320.0);
        final var year = 2022;
        final var month = 10;
        final var dateTime1 = LocalDateTime.of(year, month, 2, 9, 59, 59)
                .toInstant(ZoneOffset.UTC);
        final var dateTime2 = LocalDateTime.of(year, month, 6, 17, 10, 31)
                .toInstant(ZoneOffset.UTC);
        final var dateTime3 = LocalDateTime.of(year, month, 15, 12, 5, 0)
                .toInstant(ZoneOffset.UTC);
        final var dateTime4 = LocalDateTime.of(year, month, 29, 21, 30, 18)
                .toInstant(ZoneOffset.UTC);

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

        when(operationRepository.getAllOrderByDateDesc(accountId)).thenReturn(operationList);
        when(accountStatementFormatter.format(new Account(accountId, balance, operationList)))
                .thenReturn(expected);
        doNothing().when(stringPrinter).print(expected);

        assertDoesNotThrow(() ->bankAccountService.printAccountStatement(accountId));
    }
}
