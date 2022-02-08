package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;;

public class ExceptionHandlingFacade {
    public UserImpl handleCreateUser ( UserImpl user, Stream<User> userStream) throws UseException{
        boolean notUnique = userStream
                .anyMatch(x -> Objects.equals(x.getPersonalIdentificationNumber(), user.getPersonalIdentificationNumber()));
        if (notUnique) {
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        }
        return user;
    }

    public void handlePID(User user,  UsersRepository usersRepository, String personalIdentificationNumber) throws UseException {
        boolean isNotUnique = usersRepository.all().anyMatch(x -> x.getPersonalIdentificationNumber().equals(personalIdentificationNumber));
        if(isNotUnique) {
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        }
        user.setPersonalIdentificationNumber(personalIdentificationNumber);
        usersRepository.save(user);
    }

    public AccountImpl handlecreateAccount(AccountImpl account, AccountsRepository accountsRepository, String accountName, User user) throws UseException {
        boolean notUnique = accountsRepository.all()
                .filter(x -> Objects.equals(x.getOwner().getName(), user.getName()))
                .anyMatch(x -> Objects.equals(x.getName(), accountName));

        if (notUnique) {

            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }

        return account;
    }

    public Account handleChangeAccount (Account account, String userId) throws UseException {
        if (!account.getOwner().getId().equals(userId)){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);

        }
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }

        return account;
    }

    public void handleChangeAccountName (Account account, AccountsRepository accountsRepository, Consumer<AccountService.ChangeAccount> changeAccountConsumer) throws UseException {

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

    }

    public Account handleAddUserToAccount (Account account, User otherUser, String userId, String userIdToBeAssigned) throws UseException {
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        } else if (Objects.equals(userId, userIdToBeAssigned)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        } else if (account.getUsers()
                .anyMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
        } else if (!Objects.equals(userId, account.getOwner().getId())) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        } else {
            account.addUser(otherUser);
        }

        return account;
    }

    public Account handleInactivateAccount (Account account, User user) throws UseException {
        if (!Objects.equals(user.getId(), account.getOwner().getId())) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        } else if (!account.isActive()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        } else if (!user.isActive()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        } else {
            account.setActive(false);
        }
        return account;
    }

    public Account handleRemoveUserFromAccount (String userId, Account account, String userIdToBeAssigned) throws UseException {
        if (!Objects.equals(userId, account.getOwner().getId())) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        }
        if (account.getUsers()
                .noneMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
        }

        return account;
    }

}

