package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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

        boolean notUnique = accountsRepository.all()
                .filter(x -> Objects.equals(x.getOwner().getName(), user.getName()))
                .anyMatch(x -> Objects.equals(x.getName(), accountName));

        if (notUnique) {
            
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();

        if (!account.getOwner().getId().equals(userId)){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);

        }
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }

        changeAccountConsumer.accept(name -> {
            if (Objects.equals(name, account.getName())) {
                System.out.println("E du dum eller..?");
            } else if (accountsRepository.all()
                    .anyMatch(x -> Objects.equals(x.getName(), name))) {
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
            } else {
                account.setName(name);
                accountsRepository.save(account);
            }
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
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
     if (!searchValue.equals("")) {
            return accountsRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchValue));
        } else if (sortOrder.toString().toLowerCase(Locale.ROOT).equals("accountname") && pageSize == null) {
            return accountsRepository.all()
                    .sorted(Comparator.comparing(Account::getName));
        } else if (userId != null) {
         FindUsersFacade findUsersFacade = new FindUsersFacade();
         return findUsersFacade.allMatchedAccounts(accountsRepository, usersRepository, userId);
        } else if (pageSize != null) {
         return ListUtils.applyPage(accountsRepository.all().sorted(Comparator.comparing(Account::getName)), pageNumber, pageSize);
        }
        return accountsRepository.all();
    }

}