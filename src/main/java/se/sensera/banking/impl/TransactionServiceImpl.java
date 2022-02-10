package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TransactionServiceImpl implements TransactionService {

    static AtomicLong globalDuration = new AtomicLong();
    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    TransactionsRepository transactionsRepository;
    Consumer<Transaction> monitor;

    public TransactionServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public Transaction createTransaction(String created, String userId, String accountId, double amount) throws UseException {
        long start = System.currentTimeMillis();
        User user = usersRepository.getEntityById(userId).orElseThrow();
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        double sum = transactionsRepository.all()
                .filter(x -> Objects.equals(x.getAccount().getId(), accountId)).mapToDouble(Transaction::getAmount).sum();

        if ((sum + amount) < 0) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_FUNDED);
        }

        if (!Objects.equals(account.getOwner().getId(), userId) && account.getUserList().noneMatch(x -> Objects.equals(x.getId(), userId))) {
            throw new UseException(Activity.CREATE_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }

        try {
            Date date = dateFormatter.parse(created);
            TransactionImpl transaction = new TransactionImpl(date, user, account, amount);
            new Thread(() -> {
                monitor.accept(transaction);
            }).start();
            long duration = System.currentTimeMillis() - start;
            globalDuration.addAndGet(duration);
            return transactionsRepository.save(transaction);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public double sum(String created, String userId, String accountId) throws UseException {

        Account account = accountsRepository.getEntityById(accountId).orElseThrow();

        if (!Objects.equals(account.getOwner().getId(), userId) && account.getUserList().noneMatch(x -> Objects.equals(x.getId(), userId))) {
            throw new UseException(Activity.SUM_TRANSACTION, UseExceptionType.NOT_ALLOWED);
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date date = dateFormatter.parse(created);
            return transactionsRepository.all()
                    .filter(x -> Objects.equals(x.getAccount().getId(), accountId))
                    .filter(x -> x.getCreated().before(date))
                    .mapToDouble(Transaction::getAmount).sum();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }


        return 0;

    }

    @Override
    public void addMonitor(Consumer<Transaction> monitor) {
        this.monitor = monitor;
    }
}