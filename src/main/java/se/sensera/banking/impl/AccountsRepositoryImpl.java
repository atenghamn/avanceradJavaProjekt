package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.AccountsRepository;

import java.util.Optional;
import java.util.stream.Stream;

public class AccountsRepositoryImpl implements AccountsRepository {
    @Override
    public Optional<Account> getEntityById(String id) {
        return Optional.empty();
    }

    @Override
    public Stream<Account> all() {
        return null;
    }

    @Override
    public Account save(Account entity) {
        return null;
    }

    @Override
    public Account delete(Account entity) {
        return null;
    }
}
