import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreNameMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class, UserFriendsMapper.class,
FilmRepository.class, FilmRowMapper.class, GenreNameMapper.class, LikesRepository.class, GenreRepository.class,
RatingRepository.class})
public class UserApplicationTests {
    private final UserRepository userStorage;

    @Test
    public void testCreateUser() {
        User user = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user);
        Assertions.assertEquals(user, userStorage.getUserById(user.getId()));
    }

    @Test
    public void testUpdateUser() {
        User user = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user);
        User userUpdate = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userUpdate.setId(user.getId());
        Assertions.assertEquals(userUpdate, userStorage.update(userUpdate));
    }

    @Test
    public void testGetAllUser() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);
        List<User> allUser = new ArrayList<>();
        allUser.add(user1);
        allUser.add(user2);
        System.out.println(userStorage.allUser());
        Assertions.assertEquals(allUser, userStorage.allUser());
    }

    @Test
    public void testFindUserById() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(user1.getId()));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }
}