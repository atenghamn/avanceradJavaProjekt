package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.User;

import java.util.stream.Stream;

public class AccountImpl implements Account {

    private String name;
    private String id;
    private User owner;
    private boolean active;

    public AccountImpl(String name, String id, User owner) {
        this.name = name;
        this.id = id;
        this.owner = owner;
        this.active = true;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public User getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Stream<User> getUsers() {
        return Stream.<User>builder().build(); //FRÅGETECKEN
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void removeUser(User user) {

    }
}
