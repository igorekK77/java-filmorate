package ru.yandex.practicum.tests;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

@Component
public class InMemoryFilmStorageTests {

    private static FilmStorage filmStorage;
    private static Film film;
    private static GenreName genreName = new GenreName(1, "Test");
    private static final RatingName ratingName = new RatingName(1, "Test");


    @BeforeEach
    public void beforeEach() {
        filmStorage = new InMemoryFilmStorage();
        film = new Film("testFilm", "dTestFilm", LocalDate.of(2015,11,23),
                112, List.of(genreName), ratingName);
        genreName = new GenreName();
    }

    @Test
    public void testCreateFilm() {
        filmStorage.create(film);
        Film checkFilm = new Film("testFilm", "dTestFilm",
                LocalDate.of(2015,11,23), 112, List.of(genreName),
                ratingName);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, film);
    }

    @Test
    public void testUpdateFilm() {
        filmStorage.create(film);
        Film updateFilm = new Film("testUpdateFilm", "dTestUpdateFilm",
                LocalDate.of(2015,11,23), 112,
                List.of(genreName), ratingName);
        updateFilm.setId(1L);
        filmStorage.update(updateFilm);
        Assertions.assertEquals(updateFilm, film);
    }

    @Test
    public void testCreateFilmWithNullName() {
        Film nullNameFilm = new Film(null, "testFilm", LocalDate.of(2015,11,23),
                112, List.of(genreName), ratingName);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.create(nullNameFilm));
    }

    @Test
    public void testCreateFilmWithDescription201Characters() {
        Film testFilm = new Film("Test", "a".repeat(201),
                LocalDate.of(2015,11,23), 112,
                List.of(genreName), ratingName);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.create(testFilm));
    }

    @Test
    public void testCreateFilmWithDescription200Characters() {
        Film testFilm = new Film("Test", "a".repeat(200),
                LocalDate.of(2015,11,23), 112, List.of(genreName),
                ratingName);
        filmStorage.create(testFilm);
        Film checkFilm = new Film("Test", "a".repeat(200),
                LocalDate.of(2015,11,23), 112,
                List.of(genreName), ratingName);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, testFilm);
    }

    @Test
    public void testCreateFilmWithDescription199Characters() {
        Film testFilm = new Film("Test", "a".repeat(199),
                LocalDate.of(2015,11,23), 112,
                List.of(genreName), ratingName);
        filmStorage.create(testFilm);
        Film checkFilm = new Film("Test", "a".repeat(199),
                LocalDate.of(2015,11,23), 112,
                List.of(genreName), ratingName);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, testFilm);
    }

    @Test
    public void testCreateFilmWithBeforeCorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.create(film));
    }

    @Test
    public void testCreateFilmWithEqualsCorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.create(film));
    }

    @Test
    public void testCreateFilmWithNegativeDurationOfMovie() {
        film.setDuration(-20);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.create(film));
    }

}
