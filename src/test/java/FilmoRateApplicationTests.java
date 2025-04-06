import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Status;
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
@Import({UserRepository.class, UserRowMapper.class, UserFriendsMapper.class, FriendsRepository.class,
FilmRepository.class, FilmRowMapper.class, GenreNameMapper.class, LikesRepository.class, GenreRepository.class,
RatingRepository.class})
public class FilmoRateApplicationTests {
    private final UserRepository userStorage;
    private final FriendsRepository friendsRepository;
    private final FilmRepository filmRepository;
    private final LikesRepository likesRepository;
    private final GenreRepository genreRepository;
    private final RatingRepository ratingRepository;

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

        Assertions.assertEquals(friends ,friendsRepository.printListCommonFriends(user1.getId(), user2.getId()));
    }

    @Test
    public void testCreateFilm() {
        Film film = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film);
        Assertions.assertEquals(film, filmRepository.getFilmById(film.getId()));
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film);

        Film updateFilm = new Film("testUpdate", "testDescriptionUpdate",
                LocalDate.of(2015,11,11), 156, new RatingName(2, "PG"));
        updateFilm.setId(film.getId());
        updateFilm.setGenres(new ArrayList<>());

        Assertions.assertEquals(updateFilm, filmRepository.update(updateFilm));
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film1);

        Film film2 = new Film("testUpdate", "testDescriptionUpdate",
                LocalDate.of(2015,11,11), 156, new RatingName(2, "PG"));
        filmRepository.create(film2);

        List<Film> allFilm = new ArrayList<>();
        allFilm.add(film1);
        allFilm.add(film2);

        Assertions.assertEquals(allFilm, filmRepository.allFilms());
    }

    @Test
    public void testGetFilmById() {
        Film film1 = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film1);
        Assertions.assertEquals(film1, filmRepository.getFilmById(film1.getId()));
    }

    @Test
    public void testPutLike() {
        Film film1 = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film1);

        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);

        Set<Long> likes = new HashSet<>();
        likes.add(user1.getId());
        Film checkFilm = likesRepository.putLike(film1.getId(), user1.getId());
        film1.setLikes(likes);

        Assertions.assertEquals(film1, checkFilm);
    }

    @Test
    public void testDeleteLike() {
        Film film1 = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film1);

        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);

        likesRepository.putLike(film1.getId(), user1.getId());

        Assertions.assertEquals(film1, likesRepository.deleteLike(film1.getId(), user1.getId()));
    }

    @Test
    public void testGetTopFilmsByLikes() {
        Film film1 = new Film("test", "testDescription",
                LocalDate.of(2012, 12, 2), 127, new RatingName(2, "PG"));
        filmRepository.create(film1);
        Film film2 = new Film("testUpdate", "testDescriptionUpdate",
                LocalDate.of(2015,11,11), 156, new RatingName(2, "PG"));
        filmRepository.create(film2);

        User user1 = new User("testuser@mail.ru", "testlogin1", "test",
                LocalDate.of(2001, 10,14));
        userStorage.create(user1);
        User user2 = new User("updateEmail@yandex.ru", "updateLogin", "updateName",
                LocalDate.of(2000, 2, 20));
        userStorage.create(user2);
        User user3 = new User("testuser3@mail.ru", "testlogin3", "test3",
                LocalDate.of(2003, 1,17));
        userStorage.create(user3);

        likesRepository.putLike(film1.getId(), user1.getId());
        likesRepository.putLike(film1.getId(), user3.getId());
        likesRepository.putLike(film2.getId(), user1.getId());
        likesRepository.putLike(film2.getId(), user2.getId());
        likesRepository.putLike(film2.getId(), user3.getId());

        Set<Long> likesFirstFilm = Set.of(user1.getId(), user3.getId());
        Set<Long> likesSecondFilm = Set.of(user1.getId(), user2.getId(), user3.getId());
        film1.setLikes(likesFirstFilm);
        film2.setLikes(likesSecondFilm);

        List<Film> topLikesFilm = new ArrayList<>();
        topLikesFilm.add(film2);
        topLikesFilm.add(film1);

        Assertions.assertEquals(topLikesFilm, likesRepository.getTopFilmsByLikes(2));
    }

    @Test
    public void testGetAllGenre() {
        List<GenreName> genres = List.of(
                new GenreName(1, "Комедия"),
                new GenreName(2, "Драма"),
                new GenreName(3, "Мультфильм"),
                new GenreName(4, "Триллер"),
                new GenreName(5, "Документальный"),
                new GenreName(6, "Боевик")
        );

        Assertions.assertEquals(genres, genreRepository.allGenre());
    }

    @Test
    public void testGetGenreById() {
        GenreName thrillerGenre = new GenreName(4, "Триллер");
        Assertions.assertEquals(thrillerGenre, genreRepository.getGenreById(4));
    }

    @Test
    public void testGetAllMpa() {
        List<RatingName> ratings = List.of(
                new RatingName(1, "G"),
                new RatingName(2, "PG"),
                new RatingName(3, "PG-13"),
                new RatingName(4, "R"),
                new RatingName(5, "NC-17")
        );

        Assertions.assertEquals(ratings, ratingRepository.allRating());
    }

    @Test
    public void testGetMpaById() {
        RatingName ratingNc = new RatingName(5, "NC-17");
        Assertions.assertEquals(ratingNc, ratingRepository.getRatingById(5));
    }
}