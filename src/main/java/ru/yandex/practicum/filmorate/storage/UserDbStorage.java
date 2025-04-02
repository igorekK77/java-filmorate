package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
@Qualifier("dbUserStorage")
public class UserDbStorage implements UserStorage{
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;
    private final UserFriendsMapper userFriendsMapper;
    private final String QUERY_FOR_CREATE_USER = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?);";
    private final String QUERY_FOR_UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?;";
    private final String QUERY_FOR_GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?;";
    private final String QUERY_GET_ALL_USER = "SELECT * FROM users;";
    private final String QUERY_FOR_GET_FRIEND_AND_STATUS = "SELECT friend_id, status FROM user_friends WHERE " +
            "user_id = ?;";
    private final String QUERY_FOR_GET_ALL_USER_ID = "SELECT user_id FROM users";

    @Autowired
    public UserDbStorage (JdbcTemplate jdbcTemplate, UserRowMapper mapper, UserFriendsMapper userFriendsMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.userFriendsMapper = userFriendsMapper;
    }

    @Override
    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.info("Имя пользователя отсутствует, новое имя пользвателя: {}", user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(QUERY_FOR_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            throw new ValidationException("Не удалось сохранить данные");
        }
        user.setId(id);

        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!isIdUsersInDatabase(newUser.getId())) {
            throw new NotFoundException("Пользователь с ID = " + newUser.getId() + " не найден!");
        }

        User user = jdbcTemplate.queryForObject(QUERY_FOR_GET_USER_BY_ID, mapper, newUser.getId());

        if (newUser.getEmail() != null && !user.getEmail().equals(newUser.getEmail())) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null && !newUser.getName().equals(user.getName())) {
            user.setName(newUser.getName());
        }
        if (newUser.getLogin() != null && !newUser.getLogin().equals(user.getLogin())) {
            user.setLogin(newUser.getLogin());
        }
        if (!newUser.getBirthday().equals(user.getBirthday())) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.error("Дата рождения не может быть в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            } else {
                user.setBirthday(newUser.getBirthday());
                log.debug("Новая дата рождения пользователя с id {}: {}", user.getId(), user.getBirthday());
            }
        }

        int userUpdateRow = jdbcTemplate.update(QUERY_FOR_UPDATE_USER, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());

        if (userUpdateRow == 0) {
            throw new ValidationException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public Collection<User> allUser() {
        List<User> users = jdbcTemplate.query(QUERY_GET_ALL_USER, mapper);

        return users;
    }

    @Override
    public User getUserById(Long id) {
        if (!isIdUsersInDatabase(id)) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден!");
        }
        User user = jdbcTemplate.queryForObject(QUERY_FOR_GET_USER_BY_ID, mapper, id);
        user.setFriends(getUserFriendsFromDB(user.getId()));
        return user;
    }

    public Map<Long, String> getUserFriendsFromDB(Long id) {
        Map<Long, String> friends = new HashMap<>();
        List<UserFriends> userFriends = jdbcTemplate.query(QUERY_FOR_GET_FRIEND_AND_STATUS, userFriendsMapper, id);
        userFriends.forEach(userFriend-> friends.put(userFriend.getId(), userFriend.getStatus()));
        return friends;
    }

    private boolean isIdUsersInDatabase(Long id) {
        List<Long> allId = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_USER_ID, Long.class);
        return allId.contains(id);
    }

}
