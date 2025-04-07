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
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreNameMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmRowMapper.class, GenreNameMapper.class})
public class FilmApplicationTests {
    private final FilmRepository filmRepository;

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
}
