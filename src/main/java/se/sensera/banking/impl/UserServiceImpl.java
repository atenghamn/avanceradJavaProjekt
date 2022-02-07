package se.sensera.banking.impl;


import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.*;
import java.util.function.Consumer;

import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    ExceptionHandlingFacade exceptionHandlingFacade = new ExceptionHandlingFacade();

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(final String name, final String personalIdentificationNumber) throws UseException {
        Stream<User> userStream = usersRepository.all();
        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);
        user = exceptionHandlingFacade.handleCreateUser(user, userStream);
        return usersRepository.save(user);
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User user = getUser1(userId);
        giveName(changeUser, user);

        return user;
    }

    public void giveName (Consumer<ChangeUser> changeUser, User user){
        changeUser.accept(new ChangeUser() {
            @Override
            public void setName(String name) {
                user.setName(name);
                usersRepository.save(user);
            }
            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                exceptionHandlingFacade.handlePID(user, usersRepository, personalIdentificationNumber);
            }});
    }



    private User getUser1(String userId) throws UseException {
        return usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        User user = getUser1(userId);
        user.setActive(false);

        return usersRepository.save(user);
    }

    @Override
    public Optional<User> getUser(String userId) {
        return usersRepository.getEntityById(userId)
                .filter(x -> Objects.equals(x.getId(), userId));
    }


    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        if (Objects.equals(sortOrder.toString(), "Name")) {return sortByName(searchString);
        } else if (Objects.equals(sortOrder.toString(), "PersonalId")) {return sortByPID(searchString);
        } else if (pageNumber != null && pageNumber >= 2) {return Stream.empty();}
        else {return unSorted(searchString);}}

    private Stream<User> unSorted (String searchString){
        return usersRepository.all()
                .filter(User::isActive)
                .filter(x -> x.getName().toLowerCase().contains(searchString));

    }
    private Stream<User> sortByName (String searchString){
        return usersRepository.all()
                .filter(User::isActive)
                .filter(x -> x.getName().toLowerCase().contains(searchString))
                .sorted(Comparator.comparing(User::getName));
    }

    private Stream<User> sortByPID (String PID){
        return usersRepository.all()
                .filter(User::isActive)
                .filter(x -> x.getName().toLowerCase().contains(PID))
                .sorted(Comparator.comparing(User::getPersonalIdentificationNumber));
    }
}
