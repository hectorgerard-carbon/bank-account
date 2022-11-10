package io.gerard.services;

import io.gerard.exceptions.AccountNotFoundException;
import io.gerard.exceptions.ZeroOrNegativeAmountException;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceShould {
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private OperationRepository operationRepository;

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
}
