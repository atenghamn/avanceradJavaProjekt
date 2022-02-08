

package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class AccountServiceImpl implements AccountService {
    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    ExceptionHandlingFacade exceptionHandlingFacade;
    FindAccountsFacade findAccountsFacade;



    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository, ExceptionHandlingFacade exceptionHandlingFacade, FindAccountsFacade findAccountsFacade) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
        this.exceptionHandlingFacade = exceptionHandlingFacade;
        this.findAccountsFacade = findAccountsFacade;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {

        User user = getUser(userId);

        AccountImpl account = new AccountImpl(accountName, user, true);

        account = exceptionHandlingFacade.handlecreateAccount(account, accountsRepository, accountName, user);

        return accountsRepository.save(account);
    }

    private User getUser(String userId) throws UseException {
        return usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = getAccount(accountId);
        account = exceptionHandlingFacade.handleChangeAccount(account, userId);

        // Kolla
        Account finalAccount = account;
        changeAccountConsumer.accept(name -> {
            exceptionHandlingFacade.handleChangeAccountName(name, finalAccount, accountsRepository);
        });

        return account;
    }

    private Account getAccount(String accountId) {
        return accountsRepository.getEntityById(accountId).orElseThrow();
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {

        Account account = accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        User otherUser = getUser(userIdToBeAssigned);

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

        if (userId != null || pageSize != null) {
            return finderTwo(userId, pageNumber, pageSize);
        }
        return finderOne(searchValue, pageSize, sortOrder);
    }

    private Stream<Account> finderOne(String searchValue, Integer pageSize, SortOrder sortOrder) {
        if (!searchValue.equals("")) {
            return accountsRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchValue));
        } else if (sortOrder.toString().toLowerCase().equals("accountname") && pageSize == null) {
            return accountsRepository.all()
                    .sorted(Comparator.comparing(Account::getName));
        }
        return accountsRepository.all();
    }

    private Stream<Account> finderTwo(String userId, Integer pageNumber, Integer pageSize) {
        if (userId != null) {
            return findAccountsFacade.allMatchedAccounts(accountsRepository, usersRepository, userId);

        } else if (pageSize != null) {
            return ListUtils.applyPage(accountsRepository.all().sorted(Comparator.comparing(Account::getName)), pageNumber, pageSize);
        }
        return accountsRepository.all();
    }


}