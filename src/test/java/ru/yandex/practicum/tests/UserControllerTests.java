package ru.yandex.practicum.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserControllerTests {

    private static UserController userController;
    private static User user;

    @BeforeEach
    public void beforeEach() {
        userController = new UserController();
        user = new User("testUser@yandex.ru", "testLogin", "testName",
                LocalDate.of(2001, 10, 22));
    }

    @Test
    public void testCreateUser() {
        userController.create(user);
        User checkUser = new User("testUser@yandex.ru", "testLogin", "testName",
                LocalDate.of(2001, 10, 22));
        checkUser.setId(1L);
        Assertions.assertEquals(checkUser, user);
    }

    @Test
    public void testUpdateUser() {
        userController.create(user);
        User updateUser = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2003, 2,11));
        updateUser.setId(1L);
        userController.update(updateUser);
        Assertions.assertEquals(updateUser, user);
    }

    @Test
    public void testCreateUserWithEmptyEmail() {
        user.setEmail(null);
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithoutSpecialCharacter() {
        user.setEmail("testemailyandex.ru");
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithEmptyLogin() {
        user.setLogin(null);
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithSpace() {
        user.setLogin("test login");
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithNullName() {
        user.setName(null);
        userController.create(user);
        User checkUser = new User("testUser@yandex.ru", "testLogin", "testLogin",
                LocalDate.of(2001, 10, 22));
        checkUser.setId(1L);
        Assertions.assertEquals(checkUser, user);
    }

    @Test
    public void testCreateUserWithBirthdayInFuture() {
        user.setBirthday(LocalDate.of(2026, 11,23));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));
    }
}
