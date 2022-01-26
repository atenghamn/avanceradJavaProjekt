package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountImpl implements Account {

    private User owner;
    private List<User> userList = new ArrayList<>();
    private  String name;
    private String id;
    private boolean isActive;


    public AccountImpl(User owner, String name, boolean isActive) {
        this.owner = owner;
        this.name = name;
        this.isActive = isActive;

    }

    @Override
    public String getId() {
        return null;
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
        return this.isActive;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public Stream<User> getUserList() {
        return userList.stream();
    }

    @Override
    public void addUser(User user) {
            userList.add(user);
    }

    @Override
    public void removeUser(User user) {
            userList.remove(user);
    }
}
