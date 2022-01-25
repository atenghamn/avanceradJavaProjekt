package se.sensera.banking.impl;

import jdk.jshell.spi.ExecutionControl;
import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
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

            }
            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                user.setPersonalIdentificationNumber(personalIdentificationNumber);

            }
        });
        return usersRepository.save(user);
    }

    @Override
    public User inactivateUser(String userId) throws UseException {
        return null;
    }

    @Override
    public Optional<User> getUser(String userId) {
        return Optional.empty();
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {
        return null;
    }
}
