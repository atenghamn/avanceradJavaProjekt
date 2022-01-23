package se.sensera.banking.impl;

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

    private final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public User createUser(final String name, final String personalIdentificationNumber) throws UseException {
        Stream<User> userStream = usersRepository.all();

        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);

        boolean notUnique = userStream
                .anyMatch(x -> Objects.equals(x.getPersonalIdentificationNumber(), user.getPersonalIdentificationNumber()));
        if (notUnique) {
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else {
            return usersRepository.save(user);
        }
    }

    @Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) {

        Stream<User> userStream = usersRepository.all();
        var user= usersRepository.getEntityById(userId).get();
        changeUser.accept(user);

        user.setPersonalIdentificationNumber(userId);



        return usersRepository.save(user);

    }

    @Override
    public User inactivateUser(String userId) {
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

/*
* void update_personal_id_success() throws UseException {
        // Given
        when(usersRepository.getEntityById(eq(userId))).thenReturn(Optional.of(user));
        when(usersRepository.save(any())).then(invocation -> invocation.getArguments()[0]);
        when(usersRepository.all()).thenReturn(Stream.empty());

        // when
        userService.changeUser(userId, changeUser -> {
            try {
                changeUser.setPersonalIdentificationNumber("20011010-0234");
            } catch (UseException e) {
                throw new RuntimeException("Test failed", e);
            }
        });

        // Then
        verify(usersRepository).save(user);
        verify(user).setPersonalIdentificationNumber("20011010-0234");
        verify(user, never()).setName(anyString());
    }
* */