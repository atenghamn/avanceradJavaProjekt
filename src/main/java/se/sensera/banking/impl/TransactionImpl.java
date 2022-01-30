package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.AccountsRepository;
import se.sensera.banking.Transaction;
import se.sensera.banking.User;

import java.util.Date;
import java.util.UUID;

public class TransactionImpl implements Transaction {

    private String id;
    private Date created;
    private User user;
    private Account account;
    private double amount;

    public TransactionImpl(Date created, User user, Account account, double amount){
        this.id = UUID.randomUUID().toString();
        this.created = created;
        this.user = user;
        this.account = account;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public double getAmount() {
        return amount;
    }
}
