package se.sensera.banking.impl;

import se.sensera.banking.Account;
import se.sensera.banking.User;
import java.util.List;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class AccountImpl implements Account {

    private User owner;
    private List<User> userList = new ArrayList<>();
    private  String name;
    private String id;
    private boolean isActive;


public AccountImpl(String name, User owner, boolean isActive){
    this.name = name;
    this.owner = owner;
    this.isActive = isActive;
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
        return this.isActive;
    }

    @Override
    public void setActive(boolean active) {
    }

    @Override
    public Stream<User> getUsers() {
        return userList.stream();
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void removeUser(User user) {

    }
}
