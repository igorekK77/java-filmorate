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
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;
    private final UserFriendsMapper userFriendsMapper;
    private final String QUERY_ADDING_USER_FRIENDS = "INSERT INTO user_friends (user_id, friend_id, status) " +
            "VALUES (?, ?, " + "'" + Status.UNCONFIRMED + "'" + ");";
    private final String QUERY_DELETE_USER_FRIENDS = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?;";
    private final String QUERY_FOR_GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?;";
    private final String QUERY_FOR_GET_FRIEND_AND_STATUS = "SELECT friend_id, status FROM user_friends WHERE " +
            "user_id = ?;";
    private final String QUERY_FOR_GET_ALL_USER_ID = "SELECT user_id FROM users";

    @Autowired
    public UserRepository (JdbcTemplate jdbcTemplate, UserRowMapper mapper, UserFriendsMapper userFriendsMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.userFriendsMapper = userFriendsMapper;
    }

    private List<UserFriends> getUserFriendsFromDB(Long id) {
        return jdbcTemplate.query(QUERY_FOR_GET_FRIEND_AND_STATUS, userFriendsMapper, id);

    }

    public List<User> printListUserFriends(Long id) {
        getUserById(id);
        List<UserFriends> userFriends = jdbcTemplate.query(QUERY_FOR_GET_FRIEND_AND_STATUS, userFriendsMapper, id);
        return userFriends.stream()
                .map(userFriend -> getUserById(userFriend.getId()))
                .toList();
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        getUserById(idFirstUser);
        getUserById(idSecondUser);
        List<UserFriends> listFriendsFirstUser = getUserFriendsFromDB(idFirstUser);
        List<UserFriends> listFriendsSecondUser = getUserFriendsFromDB(idSecondUser);
        listFriendsFirstUser.retainAll(listFriendsSecondUser);
        return listFriendsFirstUser.stream()
                .map(userFriend -> getUserById(userFriend.getId()))
                .toList();
    }

    public User addFriend(Long userWhoAddedId, Long userWhomAddedId) {
        User userWhoAdded = getUserById(userWhoAddedId);
        User userWhomAdded = getUserById(userWhomAddedId);

        List<UserFriends> friendsUserWhoAdded = getUserFriendsFromDB(userWhoAddedId);

        for (UserFriends userFriends: friendsUserWhoAdded) {
            if (userFriends.getId().equals(userWhomAddedId)) {
                log.error("Пользователь с Id = {} уже добавил в друзья пользователя с Id = {}",
                        userWhomAddedId, userWhoAddedId);
                throw new ValidationException("Пользователь с Id = " + userWhomAddedId + " уже добавил в друзья пользователя" +
                        "с Id = " + userWhoAddedId);
            }
        }

        int rowCountWhoAdding = jdbcTemplate.update(QUERY_ADDING_USER_FRIENDS, userWhoAddedId, userWhomAddedId);
        if (rowCountWhoAdding == 0) {
            throw new ValidationException("Не удалось добавить пользователя в список друзей!");
        }

        userWhoAdded.setFriends(getUserFriendsFromDB(userWhoAddedId));
        return userWhoAdded;
    }

    public User deleteFriend(Long idWhoDeleted, Long idWhomDeleted) {
        User userWhoDeleted = getUserById(idWhoDeleted);
        User userWhomDeleted = getUserById(idWhomDeleted);

        List<UserFriends> friendsUserWhoAdded = getUserFriendsFromDB(idWhoDeleted);
        List<UserFriends> friendsUserWhomAdded = getUserFriendsFromDB(idWhomDeleted);

        boolean isUserHasFriends = false;

        for (UserFriends userFriends: friendsUserWhoAdded) {
            if (userFriends.getId().equals(idWhomDeleted)) {
                isUserHasFriends = true;
                break;
            }
        }

        if (!isUserHasFriends) {
            log.error("Пользователь с ID = {} не добавлял в друзья пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);

        }

        int rowCountWhoDelete =  jdbcTemplate.update(QUERY_DELETE_USER_FRIENDS, idWhoDeleted, idWhomDeleted);


        userWhoDeleted.setFriends(getUserFriendsFromDB(idWhoDeleted));

        return userWhoDeleted;
    }

    private User getUserById(Long id) {
        if (!isIdUsersInDatabase(id)) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден!");
        }
        User user = jdbcTemplate.queryForObject(QUERY_FOR_GET_USER_BY_ID, mapper, id);
        user.setFriends(getUserFriendsFromDB(user.getId()));
        return user;
    }

    private boolean isIdUsersInDatabase(Long id) {
        List<Long> allId = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_USER_ID, Long.class);
        return allId.contains(id);
    }
}
