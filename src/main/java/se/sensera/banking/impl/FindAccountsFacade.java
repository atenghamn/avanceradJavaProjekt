package se.sensera.banking.impl;

import se.sensera.banking.*;

import java.util.function.Predicate;
import java.util.stream.Stream;


public class FindAccountsFacade {
    public Stream<Account> allMatchedAccounts(AccountsRepository accountsRepository, UsersRepository usersRepository, String userId) {
        return  getUser(usersRepository, userId, accountsRepository.all());
    }

    private Stream<Account> getUser (UsersRepository usersRepository, String userId, Stream<Account> allAccounts) {
        User thisUser = usersRepository.getEntityById(userId).orElseThrow();
        return usersCompleteAccountList(allAccounts, userId, thisUser);
    }

    private Stream<Account> usersCompleteAccountList(Stream<Account> allAccounts, String userId, User thisUser){
        return allAccounts
                .filter(selectOwnerOrUser(thisUser));
    }

    public Predicate<Account> selectOwnerOrUser(User thisUser) {
        return account -> account.getUsers().anyMatch(user -> user.getId().equals(thisUser.getId())) || account.getOwner().equals(thisUser);
    }
}
