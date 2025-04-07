import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class, UserFriendsMapper.class, FriendsRepository.class})
public class FriendsApplicationTests {
    private final UserRepository userStorage;
    private final FriendsRepository friendsRepository;

    @Test
    public void testAddFriends() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);

        User checkUser = friendsRepository.addFriend(user1.getId(), user2.getId());
        List<UserFriends> friends = new ArrayList<>();
        friends.add(new UserFriends(user2.getId(), Status.UNCONFIRMED.toString()));
        user1.setFriends(friends);

        Assertions.assertEquals(friends, checkUser.getFriends());
    }

    @Test
    public void testDeleteFriends() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);

        friendsRepository.addFriend(user1.getId(), user2.getId());

        Assertions.assertEquals(user1, friendsRepository.deleteFriend(user1.getId(), user2.getId()));
    }

    @Test
    public void testGetUserFriends() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);
        friendsRepository.addFriend(user1.getId(), user2.getId());
        List<User> friends = new ArrayList<>();
        friends.add(user2);

        Assertions.assertEquals(friends, friendsRepository.printListUserFriends(user1.getId()));
    }

    @Test
    public void testGetListCommonFriends() {
        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);
        User user3 = new User("testuser3@mail.ru", "testlogin3", "test3",
                LocalDate.of(2003, 1,17));
        userStorage.create(user3);

        List<User> friends = new ArrayList<>();
        friends.add(user3);

        friendsRepository.addFriend(user1.getId(), user3.getId());
        friendsRepository.addFriend(user2.getId(), user3.getId());

        Assertions.assertEquals(friends, friendsRepository.printListCommonFriends(user1.getId(), user2.getId()));
    }
}
