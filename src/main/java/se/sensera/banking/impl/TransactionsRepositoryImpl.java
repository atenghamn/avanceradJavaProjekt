package se.sensera.banking.impl;

import se.sensera.banking.Transaction;
import se.sensera.banking.TransactionsRepository;

import java.util.Optional;
import java.util.stream.Stream;

public class TransactionsRepositoryImpl implements TransactionsRepository {
    @Override
    public Optional<Transaction> getEntityById(String id) {
        return Optional.empty();
    }

    @Override
    public Stream<Transaction> all() {
        return null;
    }

    @Override
    public Transaction save(Transaction entity) {
        return null;
    }

    @Override
    public Transaction delete(Transaction entity) {
        return null;
    }
}
