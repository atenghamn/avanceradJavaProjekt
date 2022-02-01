package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    private UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);

        boolean isNotUnique = usersRepository.all()
                .anyMatch(userCompare -> Objects.equals(userCompare.getPersonalIdentificationNumber(), user.getPersonalIdentificationNumber()));

        if (isNotUnique) {
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else {
            return usersRepository.save(user);
        }
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User user = usersRepository.getEntityById(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));

        changeUser.accept(new ChangeUser() {
            @Override
            public void setName(String name) {
                user.setName(name);
                usersRepository.save(user);
            }

            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                boolean persIdNotUnique = usersRepository.all()
                        .anyMatch(x -> x.getPersonalIdentificationNumber().equals(personalIdentificationNumber));

                if (persIdNotUnique) {
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);

                } else {
                    user.setPersonalIdentificationNumber(personalIdentificationNumber);

                    usersRepository.save(user);
                }

            }
        });
        return user;

    }

    @Override
    public User inactivateUser(String userId) throws UseException {

        User user = usersRepository.getEntityById((userId))
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
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

        if (pageNumber != null && pageNumber >= 2) {
            return Stream.empty();
        } else if (searchString.equals("")) {
            if (SortOrder.Name.equals(sortOrder)) {
                return usersRepository.all()
                        .sorted(Comparator.comparing(User::getName));
            } else if (SortOrder.PersonalId.equals(sortOrder)) {
                return usersRepository.all()
                        .sorted(Comparator.comparing(User::getPersonalIdentificationNumber));
            }
            return usersRepository.all()
                    .filter(User::isActive);

        } else {
            return usersRepository.all()
                    .filter(x -> x.getName().toLowerCase().contains(searchString));
        }


    }
}
