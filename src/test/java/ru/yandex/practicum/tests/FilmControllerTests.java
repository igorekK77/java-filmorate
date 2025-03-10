package ru.yandex.practicum.tests;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

public class FilmControllerTests {

    private static FilmController filmController;
    private static Film film;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
        film = new Film("testFilm", "dTestFilm", LocalDate.of(2015,11,23),
                112);
    }

    @Test
    public void testCreateFilm() {
        filmController.create(film);
        Film checkFilm = new Film("testFilm", "dTestFilm",
                LocalDate.of(2015,11,23), 112);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, film);
    }

    @Test
    public void testUpdateFilm() {
        filmController.create(film);
        Film updateFilm = new Film("testUpdateFilm", "dTestUpdateFilm",
                LocalDate.of(2015,11,23), 112);
        updateFilm.setId(1L);
        filmController.update(updateFilm);
        Assertions.assertEquals(updateFilm, film);
    }

    @Test
    public void testCreateFilmWithNullName() {
        Film nullNameFilm = new Film(null, "testFilm", LocalDate.of(2015,11,23),
                112);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(nullNameFilm));
    }

    @Test
    public void testCreateFilmWithDescription201Characters() {
        Film testFilm = new Film("Test", "a".repeat(201),
                LocalDate.of(2015,11,23), 112);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(testFilm));
    }

    @Test
    public void testCreateFilmWithDescription200Characters() {
        Film testFilm = new Film("Test", "a".repeat(200),
                LocalDate.of(2015,11,23), 112);
        filmController.create(testFilm);
        Film checkFilm = new Film("Test", "a".repeat(200),
                LocalDate.of(2015,11,23), 112);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, testFilm);
    }

    @Test
    public void testCreateFilmWithDescription199Characters() {
        Film testFilm = new Film("Test", "a".repeat(199),
                LocalDate.of(2015,11,23), 112);
        filmController.create(testFilm);
        Film checkFilm = new Film("Test", "a".repeat(199),
                LocalDate.of(2015,11,23), 112);
        checkFilm.setId(1L);
        Assertions.assertEquals(checkFilm, testFilm);
    }

    @Test
    public void testCreateFilmWithBeforeCorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testCreateFilmWithEqualsCorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testCreateFilmWithNegativeDurationOfMovie() {
        film.setDuration(-20);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

}
