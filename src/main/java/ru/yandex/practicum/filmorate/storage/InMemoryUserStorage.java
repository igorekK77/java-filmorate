package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> allUser() {
        return users.values();
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
            log.info("Имя пользователя отсутствует, новое имя пользвателя: {}", user.getName());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        user.setId(generateId());
        log.debug("Новый ID = {}", user.getId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь с ID = {}, информация о нем: {}", user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getName() != null && !newUser.getName().equals(oldUser.getName())) {
                oldUser.setName(newUser.getName());
                log.debug("Новое имя пользователя с id {}: {}", oldUser.getId(), oldUser.getName());
            }
            if (newUser.getLogin() != null && !newUser.getLogin().equals(oldUser.getLogin())) {
                oldUser.setLogin(newUser.getLogin());
                log.debug("Новый логин пользователя с id {}: {}", oldUser.getId(), oldUser.getLogin());
            }
            if (!newUser.getEmail().equals(oldUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
                log.debug("Новый адрес электронной почты пользователя с id {}: {}", oldUser.getId(),
                        oldUser.getEmail());
            }
            if (!newUser.getBirthday().equals(oldUser.getBirthday())) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    log.error("Дата рождения не может быть в будущем");
                    throw new ValidationException("Дата рождения не может быть в будущем");
                } else {
                    oldUser.setBirthday(newUser.getBirthday());
                    log.debug("Новая дата рождения пользователя с id {}: {}", oldUser.getId(), oldUser.getBirthday());
                }
            }
            log.info("Данные о пользователе с ID = {} обновлены, информация о пользователе: {}", oldUser.getId(),
                    oldUser);
            return oldUser;
        }
        log.error("Пользователь с ID: {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с ID: " + newUser.getId() + " не найден");
    }

    private long generateId() {
        long newId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++newId;
    }
}
