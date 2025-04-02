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

    private Map<Long, String> getUserFriendsFromDB(Long id) {
        Map<Long, String> friends = new HashMap<>();
        List<UserFriends> userFriends = jdbcTemplate.query(QUERY_FOR_GET_FRIEND_AND_STATUS, userFriendsMapper, id);
        userFriends.forEach(userFriend-> friends.put(userFriend.getId(), userFriend.getStatus()));
        return friends;
    }

    public List<User> printListUserFriends(Long id) {
        getUserById(id);
        Map<Long, String> friends = new HashMap<>();
        List<UserFriends> userFriends = jdbcTemplate.query(QUERY_FOR_GET_USER_BY_ID, userFriendsMapper, id);
        userFriends.forEach(userFriend-> friends.put(userFriend.getId(), userFriend.getStatus()));
        List<User> commonFriends = new ArrayList<>();
        friends.keySet().forEach(userId -> commonFriends.add(getUserById(userId)));
        return commonFriends;
    }

    public List<User> printListCommonFriends(Long idFirstUser, Long idSecondUser) {
        getUserById(idFirstUser);
        getUserById(idSecondUser);
        Set<Long> listFriendsFirstUser = getUserFriendsFromDB(idFirstUser).keySet();
        Set<Long> listFriendsSecondUser = getUserFriendsFromDB(idSecondUser).keySet();
        listFriendsFirstUser.retainAll(listFriendsSecondUser);
        List<User> commonFriends = new ArrayList<>();
        listFriendsFirstUser.forEach(id -> commonFriends.add(getUserById(id)));
        return commonFriends;
    }

    public User addFriend(Long userWhoAddedId, Long userWhomAddedId) {
        User userWhoAdded = getUserById(userWhoAddedId);
        User userWhomAdded = getUserById(userWhomAddedId);

        Set<Long> friendsUserWhoAdded = getUserFriendsFromDB(userWhoAddedId).keySet();
        Set<Long> friendsUserWhomAdded = getUserFriendsFromDB(userWhomAddedId).keySet();

        if (friendsUserWhoAdded.contains(userWhomAddedId) && friendsUserWhomAdded.contains(userWhoAddedId)) {
            log.error("Пользователь с Id = {} уже добавил в друзья пользователяс Id = {}",
                    userWhomAddedId, userWhoAddedId);
            throw new ValidationException("Пользователь с Id = " + userWhomAddedId + " уже добавил в друзья пользователя" +
                    "с Id = " + userWhoAddedId);
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

        Set<Long> friendsUserWhoAdded = getUserFriendsFromDB(idWhoDeleted).keySet();
        Set<Long> friendsUserWhomAdded = getUserFriendsFromDB(idWhomDeleted).keySet();

        if (!friendsUserWhoAdded.contains(idWhomDeleted) && !friendsUserWhomAdded.contains(idWhoDeleted)) {
            log.error("Пользователь с ID = {} не добавлял в друзья пользователя с Id = {}",
                    idWhoDeleted, idWhomDeleted);
            throw new ValidationException("Пользователь с ID = " + idWhoDeleted + " не добавлял в друзья " +
                    "пользователя с Id = " + idWhomDeleted);
        }

        int rowCountWhoDelete =  jdbcTemplate.update(QUERY_DELETE_USER_FRIENDS, idWhoDeleted, idWhomDeleted);
        if (rowCountWhoDelete == 0) {
            throw new ValidationException("Не удалось удалить пользователя из списа друзей!");
        }

        int rowCountWhomDelete = jdbcTemplate.update(QUERY_DELETE_USER_FRIENDS, idWhomDeleted, idWhoDeleted);
        if (rowCountWhomDelete == 0) {
            throw new ValidationException("Не удалось удалить пользователя из списа друзей!");
        }
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
