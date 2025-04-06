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
public class UserRepository implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;
    private final UserFriendsMapper userFriendsMapper;
    private final String queryForCreateUser = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?);";
    private final String queryForUpdateUser = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?;";
    private final String queryForGetUserById = "SELECT * FROM users WHERE user_id = ?;";
    private final String queryGetAllUser = "SELECT * FROM users;";
    private final String queryForGetFriendAndStatus = "SELECT friend_id, status FROM user_friends WHERE " +
            "user_id = ?;";
    private final String queryForGetAllUserId = "SELECT user_id FROM users";

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate, UserRowMapper mapper, UserFriendsMapper userFriendsMapper) {
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
            PreparedStatement ps = connection.prepareStatement(queryForCreateUser, Statement.RETURN_GENERATED_KEYS);
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

        User user = jdbcTemplate.queryForObject(queryForGetUserById, mapper, newUser.getId());

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

        int userUpdateRow = jdbcTemplate.update(queryForUpdateUser, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());

        if (userUpdateRow == 0) {
            throw new ValidationException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public Collection<User> allUser() {
        return jdbcTemplate.query(queryGetAllUser, mapper);
    }

    @Override
    public User getUserById(Long id) {
        if (!isIdUsersInDatabase(id)) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден!");
        }
        User user = jdbcTemplate.queryForObject(queryForGetUserById, mapper, id);
        user.setFriends(getUserFriendsFromDB(user.getId()));
        return user;
    }

    private List<UserFriends> getUserFriendsFromDB(Long id) {
        return jdbcTemplate.query(queryForGetFriendAndStatus, userFriendsMapper, id);
    }

    private boolean isIdUsersInDatabase(Long id) {
        List<Long> allId = jdbcTemplate.queryForList(queryForGetAllUserId, Long.class);
        return allId.contains(id);
    }

}
