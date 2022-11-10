package io.gerard.connectors;

import io.gerard.models.Account;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

class HumanReadableAccountStatementFormatterShould {

    @Test
    void returnStringStatementWithMultipleOperations() {

        HumanReadableAccountStatementFormatter sut = new HumanReadableAccountStatementFormatter();

        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");

        final var actualBalance = BigDecimal.valueOf(3320.00);

        final var year = 2022;
        final var month = 10;
        final var dateTime1 = LocalDateTime.of(year, month, 2, 9, 59, 59).toInstant(ZoneOffset.UTC);
        final var dateTime2 = LocalDateTime.of(year, month, 6, 17, 10, 31).toInstant(ZoneOffset.UTC);
        final var dateTime3 = LocalDateTime.of(year, month, 15, 12, 5, 0).toInstant(ZoneOffset.UTC);
        final var dateTime4 = LocalDateTime.of(year, month, 29, 21, 30, 18).toInstant(ZoneOffset.UTC);

        final var op1 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(2100), dateTime1);
        final var op2 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(2150), dateTime2);
        final var op3 = new Operation(UUID.randomUUID(), accountId, OperationTypes.DEPOSIT, BigDecimal.valueOf(1200.00),
                BigDecimal.valueOf(3350), dateTime3);
        final var op4 = new Operation(UUID.randomUUID(), accountId, OperationTypes.WITHDRAWAL, BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(3320.00), dateTime4);

        final var operationList = List.of(op4, op3, op2, op1);

        final var expected = """
                Your account : f910cf03-e534-4d9d-a473-94ebe3d2cae3
                	your last balance is : 3320.0
                Operations :\s
                Date 					Type 		Amount\s
                29/10/2022 23:30		WITHDRAWAL	30.0
                15/10/2022 14:05		DEPOSIT		1200.0
                06/10/2022 19:10		DEPOSIT		50.0
                02/10/2022 11:59		DEPOSIT		100.0""";

        final var actual = sut.format(new Account(accountId, actualBalance, operationList));

        Assertions.assertEquals(expected, actual);
    }
    @Test
    void returnStringStatementWithZeroOperation() {

        HumanReadableAccountStatementFormatter sut = new HumanReadableAccountStatementFormatter();

        final var accountId = UUID.fromString("f910cf03-e534-4d9d-a473-94ebe3d2cae3");

        final var actualBalance = BigDecimal.valueOf(0.0);

        List<Operation> operationList = List.of();

        final var expected = """
                Your account : f910cf03-e534-4d9d-a473-94ebe3d2cae3
                	your last balance is : 0.0
                Operations :\s
                Date 					Type 		Amount\s
                """;

        final var actual = sut.format(new Account(accountId, actualBalance, operationList));

        Assertions.assertEquals(expected, actual);
    }
}
