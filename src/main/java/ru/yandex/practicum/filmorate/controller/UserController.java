package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    Map<Long, User> users = new HashMap<>();
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> allUser() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validate(user);
        user.setId(generateId());
        logger.debug("Новый ID = {}", user.getId());
        users.put(user.getId(), user);
        logger.info("Добавлен новый пользователь с ID = {}, информация о нем: {}", user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            logger.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            validate(newUser);
            oldUser.setName(newUser.getName());
            logger.debug("Новое имя пользователя с id {}: {}", oldUser.getId(), oldUser.getName());
            oldUser.setLogin(newUser.getLogin());
            logger.debug("Новый логин пользователя с id {}: {}", oldUser.getId(), oldUser.getLogin());
            oldUser.setEmail(newUser.getEmail());
            logger.debug("Новый адрес электронной почты пользователя с id {}: {}", oldUser.getId(), oldUser.getEmail());
            oldUser.setBirthday(newUser.getBirthday());
            logger.debug("Новая дата рождения пользователя с id {}: {}", oldUser.getId(), oldUser.getBirthday());
            logger.info("Данные о пользователе с ID = {} обновлены, информация о пользователе: {}", oldUser.getId(),
                    oldUser);
            return oldUser;
        }
        logger.error("Пользователь с ID: {} не найден", newUser.getId());
        throw new ValidationException("Пользователь с ID: " + newUser.getId() + " не найден");
    }

    private long generateId() {
        long newId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++newId;
    }

    private void validate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            logger.error("Пользователь с ID = {}. Электронная почта не может быть пустой и должна содержать символ @",
                    user.getId());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            logger.error("Пользователь с ID = {}. Логин не может быть пустым и содержать пробелы", user.getId());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
            logger.info("Имя пользователя с ID = {} отсутствует, новое имя пользвателя: {}", user.getId(),
                    user.getName());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            logger.error("Пользователь с ID = {}. Дата рождения не может быть в будущем", user.getId());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }


}
