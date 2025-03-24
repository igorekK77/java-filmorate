package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        return film;
    }

    @Override
    public Collection<Film> allFilms() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Фильм с ID = {}. Название фильма не может быть пустым", film.getId());
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.error("Фильм с ID = {}. Максимальная длина описания — 200 символов", film.getId());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null ||
                !film.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
            log.error("Фильм с ID = {}. Дата релиза — не раньше 28 декабря 1895 года", film.getId());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() < 0) {
            log.error("Фильм с ID = {}. Продолжительность фильма должна быть положительным числом.", film.getId());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        film.setId(generateId());
        log.debug("Новый ID = {}", film.getId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм с ID = {}, информация о фильме: {}", film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null && !newFilm.getName().equals(oldFilm.getName())) {
                oldFilm.setName(newFilm.getName());
                log.debug("Новое название фильма с id {}: {}", oldFilm.getId(), oldFilm.getName());
            }
            if (newFilm.getDescription() != null && !newFilm.getDescription().equals(oldFilm.getDescription())) {
                if (newFilm.getDescription().length() > 200) {
                    log.error("Максимальная длина описания — 200 символов");
                    throw new ValidationException("Максимальная длина описания — 200 символов");
                } else {
                    oldFilm.setDescription(newFilm.getDescription());
                    log.debug("Новое описание фильма с id {}: {}", oldFilm.getId(), oldFilm.getDescription());
                }

            }
            if (newFilm.getDuration() != null && !newFilm.getDuration().equals(oldFilm.getDuration())) {
                oldFilm.setDuration(newFilm.getDuration());
                log.debug("Новая продолжительность фильма с id {}: {} минут", oldFilm.getId(),
                        oldFilm.getDuration());
            }
            if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(oldFilm.getReleaseDate())) {
                if (!newFilm.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
                    log.error("Дата релиза — не раньше 28 декабря 1895 года");
                    throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
                } else {
                    oldFilm.setReleaseDate(newFilm.getReleaseDate());
                    log.debug("Новая дата релиза фильма с id {}: {}", oldFilm.getId(), oldFilm.getReleaseDate());
                }
            }
            log.info("Данные о фильме с ID = {} обновлены, информация о фильме: {}", oldFilm.getId(), oldFilm);
            return oldFilm;
        }
        log.error("Фильм с ID: {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с ID: " + newFilm.getId() + " не найден");
    }

    private long generateId() {
        long newId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++newId;
    }

}
