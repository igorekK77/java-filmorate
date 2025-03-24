package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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
        User addingUser = getUserById(addingUserId);
        User userWhoAdded = getUserById(userWhoAddedId);
        if (!addingUser.getFriends().contains(userWhoAddedId) &&
                !userWhoAdded.getFriends().contains(addingUserId)) {
            addingUser.getFriends().add(userWhoAddedId);
            userWhoAdded.getFriends().add(addingUserId);
            log.info("У пользователя с ID = {} новый друг с ID = {}", addingUserId, userWhoAddedId);
        } else {
            log.error("Пользователь с Id = {} уже добавил в друзья пользователяс Id = {}",
                    addingUserId, userWhoAddedId);
            throw new ValidationException("Пользователь с Id = " + addingUserId + " уже добавил в друзья пользователя" +
                    "с Id = " + userWhoAddedId);
        }
        return addingUser;
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        User userWhoDeleted = getUserById(idWhoDeleted);
        User userWhomDeleted = getUserById(idWhomDeleted);

        if (userWhoDeleted.getFriends().contains(idWhomDeleted) &&
                userWhomDeleted.getFriends().contains(idWhoDeleted)) {
            userWhoDeleted.getFriends().remove(idWhomDeleted);
            userWhomDeleted.getFriends().remove(idWhoDeleted);
            log.info("Пользователь с Id = {} удалил из друзей пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
        } else {
            log.error("Пользователь с ID = {} не добавлял в друзья пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
        }
        return userWhoDeleted;
    }

    public List<User> printUserFriends(Long id) {
        User searchUser = getUserById(id);

        return searchUser.getFriends().stream()
                .map(this::getUserById)
                .toList();
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        List<User> listCommonFriends = new ArrayList<>();
        User firstUser = getUserById(idFirstUser);
        User secondUser = getUserById(idSecondUser);

        Set<Long> listFriendsFirstUser = new HashSet<>(firstUser.getFriends());
        Set<Long> listFriendsSecondUser = new HashSet<>(secondUser.getFriends());
        listFriendsFirstUser.retainAll(listFriendsSecondUser);

        if (listFriendsFirstUser.isEmpty()) {
            log.error("У пользователя с ID = {} нет общих друзей с пользователем с ID = {}",
                    idFirstUser, idSecondUser);
            throw new ValidationException("У пользователя с ID = " + idFirstUser + " нет общих друзей с " +
                    "пользователем с ID = " + idSecondUser);
        } else {
            for (Long id: listFriendsFirstUser) {
                listCommonFriends.add(getUserById(id));
            }
            return listCommonFriends;
        }
    }

}
