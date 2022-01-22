package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UsersRepository;

import java.util.Optional;
import java.util.stream.Stream;

public class UsersRepositoryImpl implements UsersRepository {



    @Override
    public Optional<User> getEntityById(String id) {
        return Optional.empty();
    }

    @Override
    public Stream<User> all() {
        return null;
    }

    @Override
    public User save(User entity) {
        return null;
    }

    @Override
    public User delete(User entity) {
        return null;
    }
}
