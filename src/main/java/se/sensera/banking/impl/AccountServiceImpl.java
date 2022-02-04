package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {

    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    Stream<Account> holder;

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        User andreas = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));
        verifyAccountNameUniqueness(andreas, accountName);
        AccountImpl account = new AccountImpl(accountName, andreas, true);

        return accountsRepository.save(account);
    }

    private void verifyAccountNameUniqueness(User andreas, String accountName) throws UseException {
        if (accountsRepository.all()
                .filter(x -> Objects.equals(x.getOwner().getName(), andreas.getName()))
                .anyMatch(x -> Objects.equals(x.getName(), accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {

        Account account = fetchAccount(accountId, 0);
        checkForAccountFailure(account, userId);
        checkAccountMatchUnique(changeAccountConsumer, account);
        return account;
    }


    private void checkAccountMatchUnique(Consumer<ChangeAccount> changeAccountConsumer, Account account) {
        changeAccountConsumer.accept(name -> {
            if (Objects.equals(name, account.getName())) {
                System.out.println("Den tog sig inte igenom första kollen");
            } else if (accountsRepository.all().anyMatch(x -> Objects.equals(x.getName(), name))) {
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
            } else {
                account.setName(name);
                accountsRepository.save(account);
            }
        });

    }


    private void checkForAccountFailure(Account account, String userId) throws UseException {
        if (!account.getOwner().getId().equals(userId)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = fetchAccount(accountId, 1);
        User otherUser = fetchUser(userIdToBeAssigned, 0);

        checkForAccountFailures(userId, userIdToBeAssigned, account);
        account.addUser(otherUser);

        return accountsRepository.save(account);
    }

    private void checkForAccountFailures(String userId, String userIdToBeAssigned, Account account) throws UseException {
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        }
        if (Objects.equals(userId, userIdToBeAssigned)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        }
        if (account.getUsers().anyMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
        }
        if (!Objects.equals(userId, account.getOwner().getId())) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = fetchAccount(accountId, 0);
        User otherUser = fetchUser(userIdToBeAssigned, 0);

        checkForUserFailure(userId, userIdToBeAssigned, account);
        account.removeUser(otherUser);
        return accountsRepository.save(account);
    }

    private void checkForUserFailure(String userId, String userIdToBeAssigned, Account account) throws UseException {
        if (!Objects.equals(userId, account.getOwner().getId())) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (account.getUsers().noneMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
        }
    }

    private User fetchUser(String userIdToBeAssigned, int route) throws UseException {

        if (route == 1) {
            return usersRepository.getEntityById(userIdToBeAssigned).orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
        } else if (route == 2) {
            return usersRepository.getEntityById(userIdToBeAssigned).orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));
        }
        return usersRepository.getEntityById(userIdToBeAssigned).orElseThrow();
    }

    private Account fetchAccount(String accountId, int route) throws UseException {

        if (route == 1) {
            return accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        } else if (route == 2) {
            return accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        }
        return accountsRepository.getEntityById(accountId).orElseThrow();
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        User user = fetchUser(userId, 2);
        Account account = fetchAccount(accountId, 2);

        checkForAccountExceptions(user, account);
        account.setActive(false);
        return accountsRepository.save(account);
    }

    private void checkForAccountExceptions(User user, Account account) throws UseException {
        if (!Objects.equals(user.getId(), account.getOwner().getId())) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (!account.isActive() || !user.isActive()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {

        holder = accountsRepository.all();
        finderOne(searchValue, userId, pageSize, sortOrder);
        finderTwo(userId,pageNumber,pageSize);


        return holder;

    }

    private void finderOne(String searchValue, String userId, Integer pageSize, SortOrder sortOrder) {
        if (!searchValue.equals("")) {
            holder = accountsRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchValue));
        } else if (sortOrder.toString().toLowerCase().equals("accountname") && pageSize == null) {
            holder = accountsRepository.all()
                    .sorted(Comparator.comparing(Account::getName));
        }
    }

        private void finderTwo(String userId, Integer pageNumber, Integer pageSize){
            if (userId != null) {
                holder = checkForAssociateAccounts(userId);

            } else if (pageSize != null) {
                holder = ListUtils.applyPage(accountsRepository.all().sorted(Comparator.comparing(Account::getName)), pageNumber, pageSize);
            }
        }

    private Stream<Account> checkForAssociateAccounts(String userId) {
        List<Account> usersAssociatedAccounts = new ArrayList<>();
        List<Account> allAccountsInTheBank = accountsRepository.all().collect(Collectors.toList());

       checkForOwnerShip(allAccountsInTheBank, userId, usersAssociatedAccounts);
       return usersAssociatedAccounts.stream();
}

    private void checkForOwnerShip(List<Account> allAccountsInTheBank, String userId, List<Account> usersAssociatedAccounts) {
        for (Account account : allAccountsInTheBank) {
            if (Objects.equals(account.getOwner().getId(), userId)) {
                usersAssociatedAccounts.add(account);
            }
             checkIfUserAssociatedWithAccount(account, userId, usersAssociatedAccounts);
        }
    }

    private void checkIfUserAssociatedWithAccount (Account account, String userId, List<Account> usersAssociatedAccounts) {
        List<User> userList = account.getUsers().collect(Collectors.toList());
        if (!userList.isEmpty()) {
            for (User user : userList) {
                if (Objects.equals(user.getId(), userId)) {
                    usersAssociatedAccounts.add(account);
                }
            }
        }
    }
}


