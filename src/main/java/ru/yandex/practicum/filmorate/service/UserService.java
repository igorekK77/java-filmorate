package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    public Collection<User> allUser() {
        return userStorage.allUser();
    }

    public User addFriend(Long addingUserId, Long userWhoAddedId) {
        User addingUser;
        User userWhoAdded;

        Optional<User> addingUserOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(addingUserId))
                .findFirst();
        Optional<User> userWhoAddedOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(userWhoAddedId))
                .findFirst();

        if (addingUserOptional.isEmpty()) {
            log.error("Пользователя с ID = {} не существует! " +
                    "Проверьте правильность введенных ID", addingUserId);
            throw new NotFoundException("Пользователя с ID = " + addingUserId + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            addingUser = addingUserOptional.get();
        }

        if (userWhoAddedOptional.isEmpty()) {
            log.error("Пользователя с ID = {} не существует! " +
                    "Проверьте правильность введенных ID", userWhoAddedId);
            throw new NotFoundException("Пользователя с ID = " + userWhoAddedId + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            userWhoAdded = userWhoAddedOptional.get();
        }

        if (!addingUser.getFriends().contains(userWhoAddedId) &&
                !userWhoAdded.getFriends().contains(addingUserId)) {
            addingUser.getFriends().add(userWhoAddedId);
            userWhoAdded.getFriends().add(addingUserId);
            log.info("У пользователя с ID = {} новый друг с ID = {}", addingUserId, userWhoAddedId);

            userStorage.update(addingUser);
            userStorage.update(userWhoAdded);
        } else {
            log.error("Пользователь с Id = {} уже добавил в друзья пользователяс Id = {}",
                    addingUserId, userWhoAddedId);
            throw new ValidationException("Пользователь с Id = " + addingUserId + " уже добавил в друзья пользователя" +
                    "с Id = " + userWhoAddedId);
        }
        return addingUser;
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        User userWhoDeleted;
        User userWhomDeleted;

        Optional<User> userWhoDeletedOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(idWhoDeleted))
                .findFirst();
        Optional<User> userWhomDeletedOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(idWhomDeleted))
                .findFirst();

        if (userWhoDeletedOptional.isEmpty()) {
            log.error("Пользователя с Id = {} не существует! " +
                    "Проверьте правильность введенных ID", idWhoDeleted);
            throw new NotFoundException("Пользователя с ID = " + idWhoDeleted + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            userWhoDeleted = userWhoDeletedOptional.get();
        }

        if (userWhomDeletedOptional.isEmpty()) {
            log.error("Пользователя с Id = {} не существует! " +
                    "Проверьте правильность введенных ID", idWhomDeleted);
            throw new NotFoundException("Пользователя с ID = " + idWhomDeleted + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            userWhomDeleted = userWhomDeletedOptional.get();
        }

        if (userWhoDeleted.getFriends().contains(idWhomDeleted) &&
                userWhomDeleted.getFriends().contains(idWhoDeleted)) {
            userWhoDeleted.getFriends().remove(idWhomDeleted);
            userWhomDeleted.getFriends().remove(idWhoDeleted);

            userStorage.update(userWhoDeleted);
            userStorage.update(userWhomDeleted);
            log.info("Пользователь с Id = {} удалил из друзей пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
        } else {
            log.error("Пользователь с ID = {} не добавлял в друзья пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
            throw new NoContentException("Пользователь с ID = " + idWhoDeleted + " не добавлял в друзья пользователя" +
                    " с Id = " + idWhomDeleted);
        }
        return userWhoDeleted;
    }

    public List<User> printUserFriends(Long id) {
        User searchUser;
        Optional<User> searchUserOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();

        if (searchUserOptional.isEmpty()) {
            log.error("Пользователь с ID = {} не существует", id);
            throw new NotFoundException("Пользователь с ID = " + id + " не существует");
        } else {
            searchUser = searchUserOptional.get();
        }

        List<Optional<User>> totalUserFriendsOptional = searchUser.getFriends().stream()
                .map(this::searchUserById)
                .toList();

        List<User> totalUserFriends = new ArrayList<>();
        for (Optional<User> user: totalUserFriendsOptional) {
            user.ifPresent(totalUserFriends::add);
        }
        return totalUserFriends;

    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        List<User> listCommonFriends = new ArrayList<>();
        User firstUser;
        User secondUser;

        Optional<User> firstUserOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(idFirstUser))
                .findFirst();
        Optional<User> secondUserOptional = userStorage.allUser().stream()
                .filter(user -> user.getId().equals(idSecondUser))
                .findFirst();

        if (firstUserOptional.isEmpty()) {
            log.error("Пользователя с id = {} не существует! " +
                    "Проверьте правильность введенных ID", idFirstUser);
            throw new NotFoundException("Пользователя с ID = " + idFirstUser + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            firstUser = firstUserOptional.get();
        }

        if (secondUserOptional.isEmpty()) {
            log.error("Пользователя с id = {} не существует! " +
                    "Проверьте правильность введенных ID", idSecondUser);
            throw new NotFoundException("Пользователя с ID = " + idSecondUser + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        } else {
            secondUser = secondUserOptional.get();
        }

        List<Long> listIdCommonFriends = firstUser.getFriends().stream()
                .filter(id -> secondUser.getFriends().contains(id))
                .toList();

        if (listIdCommonFriends.isEmpty()) {
            log.error("У пользователя с ID = {} нет общих друзей с пользователем с ID = {}",
                    idFirstUser, idSecondUser);
            throw new ValidationException("У пользователя с ID = " + idFirstUser + " нет общих друзей с " +
                    "пользователем с ID = " + idSecondUser);
        } else {
            for (Long id: listIdCommonFriends) {
                Optional<User> userOptional = searchUserById(id);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    listCommonFriends.add(user);
                    log.trace("В финальный список добавлен пользователь: {}", user);
                }
            }
        }
        return listCommonFriends;
    }

    private Optional<User> searchUserById(Long userId) {
        return userStorage.allUser().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }
}
