import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreNameMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class, UserFriendsMapper.class,
        FilmRepository.class, FilmRowMapper.class, GenreNameMapper.class, LikesRepository.class})
public class LikesApplicationTests {
    private final FilmRepository filmRepository;
    private final LikesRepository likesRepository;
    private final UserRepository userStorage;

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
        film1.setGenres(null);

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
        film1.setGenres(null);

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
        film1.setGenres(null);
        film2.setGenres(null);
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
}
