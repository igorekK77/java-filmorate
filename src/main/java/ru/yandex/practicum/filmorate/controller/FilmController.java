package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    Map<Long, Film> films = new HashMap<>();
    private final static Logger logger = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> allFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(generateId());
        logger.debug("Новый ID = {}", film.getId());
        films.put(film.getId(), film);
        logger.info("Добавлен новый фильм с ID = {}, информация о фильме: {}", film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            logger.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            validate(newFilm);
            oldFilm.setName(newFilm.getName());
            logger.debug("Новое название фильма с id {}: {}" , oldFilm.getId(), oldFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            logger.debug("Новое описание фильма с id {}: {}", oldFilm.getId(), oldFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            logger.debug("Новая продолжительность фильма с id {}: {} минут", oldFilm.getId(),
                    oldFilm.getDuration().toMinutes());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            logger.debug("Новая дата релиза фильма с id {}: {}", oldFilm.getId(), oldFilm.getReleaseDate());
            logger.info("Данные о фильме с ID = {} обновлены, информация о фильме: {}", oldFilm.getId(), oldFilm);
            return oldFilm;
        }
        logger.error("Фильм с ID: {} не найден", newFilm.getId());
        throw new ValidationException("Фильм с ID: " + newFilm.getId() + " не найден");
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            logger.error("Фильм с ID = {}. Название фильма не может быть пустым", film.getId());
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            logger.error("Фильм с ID = {}. Максимальная длина описания — 200 символов", film.getId());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) ||
                film.getReleaseDate().equals(LocalDate.of(1895, 12, 28))) {
            logger.error("Фильм с ID = {}. Дата релиза — не раньше 28 декабря 1895 года", film.getId());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration().toSeconds() < 0) {
            logger.error("Фильм с ID = {}. Продолжительность фильма должна быть положительным числом.", film.getId());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    private long generateId() {
        long newId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++newId;
    }
}
