package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserRepository;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserStorage userStorage, UserRepository userRepository) {
        this.userStorage = userStorage;
        this.userRepository = userRepository;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> allUser() {
        return userStorage.allUser();
    }

    public User addFriend(Long addingUserId, Long userWhoAddedId) {
        return userRepository.addFriend(addingUserId, userWhoAddedId);
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        return userRepository.deleteFriend(idWhoDeleted, idWhomDeleted);
    }

    public List<User> printUserFriends(Long id) {
        return userRepository.printListUserFriends(id);
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        return userRepository.printListCommonFriends(idFirstUser, idSecondUser);
    }

}
