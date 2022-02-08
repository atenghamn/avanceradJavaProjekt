package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class AccountServiceImpl implements AccountService {

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    ExceptionHandlingFacade exceptionHandlingFacade = new ExceptionHandlingFacade();


    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {

        User user = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));

        AccountImpl account = new AccountImpl(user, accountName, true);

        account = exceptionHandlingFacade.handlecreateAccount(account, accountsRepository, accountName, user);

        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();
        account = exceptionHandlingFacade.handleChangeAccount(account, userId);

        Account finalAccount = account;
        changeAccountConsumer.accept(name -> {
            exceptionHandlingFacade.handleChangeAccountName(name, finalAccount, accountsRepository);
        });

        return account;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {

        Account account = accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        User otherUser = usersRepository.getEntityById(userIdToBeAssigned).orElseThrow();

        account = exceptionHandlingFacade.handleAddUserToAccount(account, otherUser, userId, userIdToBeAssigned);

        return accountsRepository.save(account);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {

        Account account = accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        User otherUser = usersRepository.getEntityById(userIdToBeAssigned).orElseThrow();

        account = exceptionHandlingFacade.handleRemoveUserFromAccount(userId, account, userIdToBeAssigned);

        account.removeUser(otherUser);

        return accountsRepository.save(account);
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        User user = usersRepository.getEntityById(userId).orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));
        Account account = accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        account = exceptionHandlingFacade.handleInactivateAccount(account, user);

        return accountsRepository.save(account);
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
     if (!searchValue.equals("")) {
            return accountsRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchValue));
        } else if (sortOrder.toString().toLowerCase(Locale.ROOT).equals("accountname") && pageSize == null) {
            return accountsRepository.all()
                    .sorted(Comparator.comparing(Account::getName));
        } else if (userId != null) {
         FindAccountsFacade findAccountsFacade = new FindAccountsFacade();
         return findAccountsFacade.allMatchedAccounts(accountsRepository, usersRepository, userId);
        }
        return accountsRepository.all();
    }

}