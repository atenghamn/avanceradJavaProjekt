package se.sensera.banking.impl;

import se.sensera.banking.*;
import java.util.stream.Stream;

public class FindUsersFacade {
    // Första metoden hämtar alla konton och lägger dem i variabeln allAccounts
    public Stream<Account> allMatchedAccounts(AccountsRepository accountsRepository, UsersRepository usersRepository, String userId) {
       Stream<Account> allAccounts = accountsRepository.all();
       allAccounts = getUser(usersRepository, userId, allAccounts); // Kallar på metoden getUser för att välja en användare
       return allAccounts; // Skicka tillbaka kontona till objektet som kallat
    }

    private Stream<Account> getUser (UsersRepository usersRepository, String userId, Stream<Account> allAccounts) {

            User thisUser = usersRepository.getEntityById(userId).orElseThrow(); // Plockar ut en användare
            allAccounts = usersCompleteAccountList(allAccounts, userId, thisUser); // kallar på metoden som hämtaren vår användares alla konto
        return allAccounts; // Skickar tillbaka till allMatchedAccounts som i sin tur skickar tillbaka till ursprungsobjektet
    }

    private Stream<Account> usersCompleteAccountList(Stream<Account> allAccounts, String userId, User thisUser){
        return allAccounts // Returnerar användarens alla konto till getuser. Skulle kunna ligga i samma metod men tänker seperation of concerns, att varje metod bara ska göra en sak
                .filter(account -> account.getUserList().anyMatch(user -> user.getId().equals(userId)) || account.getOwner().equals(thisUser));
    }
}
