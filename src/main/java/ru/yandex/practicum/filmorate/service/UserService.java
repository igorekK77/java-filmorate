package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        User addingUser = userStorage.getUsers().get(addingUserId);
        User userWhoAdded = userStorage.getUsers().get(userWhoAddedId);
        if (addingUser != null) {
            if (userWhoAdded != null) {
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
            } else {
                log.error("Пользователя с ID = {} не существует. " +
                        "Проверьте правильность введенных ID", userWhoAddedId);
                throw new NotFoundException("Пользователя с ID = " + userWhoAddedId + " не существует! " +
                        "Проверьте правильность введеннего ID!");
            }
        } else {
            log.error("Пользователя с ID = {} не существует! " +
                    "Проверьте правильность введенных ID", addingUserId);
            throw new NotFoundException("Пользователя с ID = " + addingUserId + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        }
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        User userWhoDeleted = userStorage.getUsers().get(idWhoDeleted);
        User userWhomDeleted = userStorage.getUsers().get(idWhomDeleted);

        if (userWhoDeleted != null) {
            if (userWhomDeleted != null) {
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
            } else {
                log.error("Пользователя с Id = {} не существует! " +
                        "Проверьте правильность введенных Id", idWhomDeleted);
                throw new NotFoundException("Пользователя с ID = " + idWhomDeleted + " не существует! " +
                        "Проверьте правильность введеннего ID!");
            }
        } else {
            log.error("Пользователя с Id = {} не существует! " +
                    "Проверьте правильность введенных ID", idWhoDeleted);
            throw new NotFoundException("Пользователя с ID = " + idWhoDeleted + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        }
    }

    public List<User> printUserFriends(Long id) {
        User searchUser = userStorage.getUsers().get(id);

        if (searchUser != null) {
            List<Optional<User>> totalUserFriendsOptional = searchUser.getFriends().stream()
                    .map(this::searchUserById)
                    .toList();

            List<User> totalUserFriends = new ArrayList<>();
            for (Optional<User> user: totalUserFriendsOptional) {
                user.ifPresent(totalUserFriends::add);
            }
            return totalUserFriends;
        } else {
            log.error("Пользователь с ID = {} не существует", id);
            throw new NotFoundException("Пользователь с ID = " + id + " не существует");
        }
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        List<User> listCommonFriends = new ArrayList<>();
        User firstUser = userStorage.getUsers().get(idFirstUser);
        User secondUser = userStorage.getUsers().get(idSecondUser);

        if (firstUser != null) {
            if (secondUser != null) {
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
                        if (searchUserById(id).isPresent()) {
                            listCommonFriends.add(searchUserById(id).get());
                        }
                    }
                    return listCommonFriends;
                }
            } else {
                log.error("Пользователя с id = {} не существует! " +
                        "Проверьте правильность введенных Id", idSecondUser);
                throw new NotFoundException("Пользователя с ID = " + idSecondUser + " не существует! " +
                        "Проверьте правильность введеннего ID!");
            }
        } else {
            log.error("Пользователя с id = {} не существует! " +
                    "Проверьте правильность введенных ID", idFirstUser);
            throw new NotFoundException("Пользователя с ID = " + idFirstUser + " не существует! " +
                    "Проверьте правильность введеннего ID!");
        }

    }

    private Optional<User> searchUserById(Long userId) {
        return userStorage.allUser().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }
}
