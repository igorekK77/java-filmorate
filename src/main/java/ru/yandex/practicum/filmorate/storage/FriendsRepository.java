package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.*;

@Slf4j
@Repository
public class FriendsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;
    private final UserFriendsMapper userFriendsMapper;
    private final String queryAddingUserFriends = "INSERT INTO user_friends (user_id, friend_id, status) " +
            "VALUES (?, ?, " + "'" + Status.UNCONFIRMED + "'" + ");";
    private final String queryForGetUserById = "SELECT * FROM users WHERE user_id = ?;";
    private final String queryForGetFriendAndStatus = "SELECT friend_id, status FROM user_friends WHERE " +
            "user_id = ?;";
    private final String queryForGetAllUserId = "SELECT user_id FROM users";

    @Autowired
    public FriendsRepository(JdbcTemplate jdbcTemplate, UserRowMapper mapper, UserFriendsMapper userFriendsMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.userFriendsMapper = userFriendsMapper;
    }

    private List<UserFriends> getUserFriendsFromDB(Long id) {
        return jdbcTemplate.query(queryForGetFriendAndStatus, userFriendsMapper, id);

    }

    public List<User> printListUserFriends(Long id) {
        getUserById(id);
        return jdbcTemplate.query("SELECT u.*\n" +
                "FROM USERS u \n" +
                "WHERE u.USER_ID IN (SELECT FRIEND_ID FROM USER_FRIENDS uf WHERE USER_ID = ?);", mapper, id);
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        getUserById(idFirstUser);
        getUserById(idSecondUser);
        List<User> firstUserFriends = printListUserFriends(idFirstUser);
        List<User> secondUserFriends = printListUserFriends(idSecondUser);
        firstUserFriends.retainAll(secondUserFriends);
        return firstUserFriends;

    }

    public User addFriend(Long userWhoAddedId, Long userWhomAddedId) {
        User userWhoAdded = getUserById(userWhoAddedId);
        User userWhomAdded = getUserById(userWhomAddedId);
        int rowCountWhoAdding = jdbcTemplate.update(queryAddingUserFriends, userWhoAddedId, userWhomAddedId);
        if (rowCountWhoAdding == 0) {
            log.error("Пользователь с Id = {} уже добавил в друзья пользователя с Id = {}",
                    userWhomAddedId, userWhoAddedId);
            throw new ValidationException("Пользователь с Id = " + userWhomAddedId + " уже добавил в друзья пользователя" +
                    "с Id = " + userWhoAddedId);
        }
        userWhoAdded.setFriends(getUserFriendsFromDB(userWhoAddedId));
        return userWhoAdded;
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        User userWhoDeleted = getUserById(idWhoDeleted);
        User userWhomDeleted = getUserById(idWhomDeleted);
        int rowCount = jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?",
                idWhoDeleted, idWhomDeleted);
        if (rowCount == 0) {
            log.error("Пользователь с ID = {} не добавлял в друзья пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
        }
        userWhoDeleted.setFriends(getUserFriendsFromDB(idWhoDeleted));
        return userWhoDeleted;
    }

    private User getUserById(Long id) {
        if (!isIdUsersInDatabase(id)) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден!");
        }
        User user = jdbcTemplate.queryForObject(queryForGetUserById, mapper, id);
        user.setFriends(getUserFriendsFromDB(user.getId()));
        return user;
    }

    private boolean isIdUsersInDatabase(Long id) {
        List<Long> allId = jdbcTemplate.queryForList(queryForGetAllUserId, Long.class);
        return allId.contains(id);
    }
}
