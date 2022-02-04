package se.sensera.banking.impl;

import jdk.jshell.spi.ExecutionControl;
import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.Comparator;
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
        Stream<User> userStream = usersRepository.all();
        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);
        boolean notUnique = userStream.anyMatch(x -> Objects.equals(x.getPersonalIdentificationNumber(), user.getPersonalIdentificationNumber()));
        return ifNotUnique(user, notUnique, 1, personalIdentificationNumber);
    }

    private User ifNotUnique(User user, boolean notUnique, int route, String personalIdentificationNumber) throws UseException {
        if (notUnique && route ==1) {throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else if (notUnique && route == 2) {throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else {
            user.setPersonalIdentificationNumber(personalIdentificationNumber);
            return usersRepository.save(user);
        }
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
                boolean isNotUnique = usersRepository.all().anyMatch(x -> x.getPersonalIdentificationNumber().equals(personalIdentificationNumber));
                ifNotUnique(user, isNotUnique, 2, personalIdentificationNumber);
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
        if (Objects.equals(sortOrder.toString(), "Name")) {
            return usersRepository.all()
                    .filter(User::isActive)
                    .filter(x -> x.getName().toLowerCase().contains(searchString))
                    .sorted(Comparator.comparing(User::getName));
        } else if (Objects.equals(sortOrder.toString(), "PersonalId")) {
            return usersRepository.all()
                    .filter(User::isActive)
                    .filter(x -> x.getName().toLowerCase().contains(searchString))
                    .sorted(Comparator.comparing(User::getPersonalIdentificationNumber));
        } else {
            if(pageNumber != null && pageNumber >= 2){
                return Stream.empty();
            }
            return usersRepository.all()
                    .filter(User::isActive)
                    .filter(x -> x.getName().toLowerCase().contains(searchString));
        }
    }
}
