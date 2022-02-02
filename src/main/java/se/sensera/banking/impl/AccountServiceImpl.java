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


        Account account = accountsRepository.getEntityById(accountId).orElseThrow();

        if (!account.getOwner().getId().equals(userId)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }

        changeAccountConsumer.accept(name -> {
            if (Objects.equals(name, account.getName())) {
                System.out.println("E du dum eller?");
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

        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        }
        if (Objects.equals(userId, userIdToBeAssigned)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        }
        if (account.getUsers()
                .anyMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
        }
        if (!Objects.equals(userId, account.getOwner().getId())) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        account.addUser(otherUser);

        return accountsRepository.save(account);

    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).orElseThrow(() -> new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND));
        User otherUser = usersRepository.getEntityById(userIdToBeAssigned).orElseThrow();

        if (!Objects.equals(userId, account.getOwner().getId())){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (account.getUsers()
                .noneMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
        }
        account.removeUser(otherUser);
        return accountsRepository.save(account);
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        User user = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND));
        Account account = accountsRepository.getEntityById(accountId)
                .orElseThrow(() -> new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND));

        if (!Objects.equals(user.getId(), account.getOwner().getId())) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (!account.isActive() || !user.isActive()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }
        account.setActive(false);
        return accountsRepository.save(account);
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {

        if(!searchValue.equals("")){
            return accountsRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchValue));
        } else if (sortOrder.toString().toLowerCase().equals("accountname") && pageSize == null){
            return accountsRepository.all()
                    .sorted(Comparator.comparing(Account::getName));
        } else if (userId != null){
            List<Account> usersAssociatedAccounts = new ArrayList<>();
            List<Account> allAccountsInTheBank = accountsRepository.all().collect(Collectors.toList());

            for(Account account : allAccountsInTheBank) {
                if(Objects.equals(account.getOwner().getId(), userId)){
                    usersAssociatedAccounts.add(account);
                }
                List<User> userList = account.getUsers().collect(Collectors.toList());
                if (!userList.isEmpty()){
                    for (User user : userList){
                        if (Objects.equals(user.getId(), userId)){
                            usersAssociatedAccounts.add(account);
                        }
                    }
                }
            }

            return usersAssociatedAccounts.stream();

        } else if(pageSize != null) {
            return ListUtils.applyPage(accountsRepository.all().sorted(Comparator.comparing(Account::getName)), pageNumber, pageSize);
        }
        return accountsRepository.all();
    }
}
