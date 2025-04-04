package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendsRepository;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendsRepository friendsRepository;

    @Autowired
    public UserService(@Qualifier("dbUserStorage") UserStorage userStorage, FriendsRepository friendsRepository) {
        this.userStorage = userStorage;
        this.friendsRepository = friendsRepository;
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
        return friendsRepository.addFriend(addingUserId, userWhoAddedId);
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        return friendsRepository.deleteFriend(idWhoDeleted, idWhomDeleted);
    }

    public List<User> printUserFriends(Long id) {
        return friendsRepository.printListUserFriends(id);
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        return friendsRepository.printListCommonFriends(idFirstUser, idSecondUser);
    }

}
