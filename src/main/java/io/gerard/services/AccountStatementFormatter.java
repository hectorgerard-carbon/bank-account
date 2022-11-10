package io.gerard.services;

import io.gerard.models.Account;

public interface AccountStatementFormatter {
    String format(Account account);
}
