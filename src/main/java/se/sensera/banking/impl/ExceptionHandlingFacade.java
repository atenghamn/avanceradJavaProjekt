package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.User;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Objects;

public class ExceptionHandlingFacade {

    public Account handleAddUserToAccount (Account account, User otherUser, String userId, String userIdToBeAssigned) throws UseException {
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        } else if (Objects.equals(userId, userIdToBeAssigned)) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        } else if (account.getUserList()
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
        if (account.getUserList()
                .noneMatch(x -> Objects.equals(x.getId(), userIdToBeAssigned))) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
        }

        return account;
    }
}
