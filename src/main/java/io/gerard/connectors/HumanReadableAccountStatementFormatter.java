package io.gerard.connectors;

import io.gerard.models.Account;
import io.gerard.models.Operation;
import io.gerard.models.OperationTypes;
import io.gerard.services.AccountStatementFormatter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class HumanReadableAccountStatementFormatter implements AccountStatementFormatter {
    @Override
    public String format(Account account) {
        final var stringOperations = String.join("\n", convertOperationsToStrings(account.operations()));
        return "Your account : " + account.accountId() + "\n" +
               "\tyour last balance is : " + account.balance() + "\n" +
               "Operations : \n" +
               "Date \t\t\t\t\tType \t\tAmount \n" +
               stringOperations;
    }

    private List<String> convertOperationsToStrings(List<Operation> operations) {
        return operations.stream().map(operation ->
        {
            final var alignAmount = (operation.operationType().equals(OperationTypes.DEPOSIT)) ? "\t\t" : "\t";
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
                    .withLocale( Locale.FRANCE )
                    .withZone( ZoneId.systemDefault() );
            return formatter.format(operation.dateTime()) + "\t\t" +
                   operation.operationType() +
                   alignAmount +
                   operation.amount();
        }).toList();
    }
}
