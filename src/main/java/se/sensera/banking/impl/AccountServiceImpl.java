package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
    User andreas = usersRepository.getEntityById(userId)
            .orElseThrow(() -> new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));

    AccountImpl account = new AccountImpl(accountName, andreas, true);

    boolean notUnique = accountsRepository.all()
            .filter(x -> Objects.equals(x.getOwner().getName(), andreas.getName()))
            .anyMatch(x -> Objects.equals(x.getName(), account.getName()));

    if (notUnique){
        throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
    }


    return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {

        // Hämta rätt account
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();


        changeAccountConsumer.accept(name -> {
            if(Objects.equals(name, account.getName())){
                System.out.println("E du dum eller?");
            }
            else if(accountsRepository.all()
                    .anyMatch(x -> Objects.equals(x.getName(), name))){
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
            }
            else {
                account.setName(name);
                accountsRepository.save(account);
            }
        });

        return account;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        return null;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
