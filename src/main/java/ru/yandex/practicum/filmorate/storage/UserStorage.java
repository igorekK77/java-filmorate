package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {
    public User create(User user);

    public User update(User newUser);

    public Collection<User> allUser();

    public Map<Long, User> getUsers();
}
