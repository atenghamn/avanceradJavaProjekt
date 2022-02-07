package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Objects;

public class AccountExceptionHandlingFacade {

public Account handleAddUserToAccount(Account account, String userId, String userIdToBeAssigned) throws UseException{
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
    return account;

}
}
