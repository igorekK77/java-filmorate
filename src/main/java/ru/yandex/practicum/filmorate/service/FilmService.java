package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.FilmRatingComparator;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film putLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь с ID = {} поставил лайк фильму: {}", userId, film);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);
        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.info("Пользователь с ID = {} удалил лайк фильму: {}", userId, film);
        } else {
            log.error("Пользователь с ID = {} не ставил лайк фильму: {}", userId, film);
            throw new ValidationException("Пользователь с ID = " + userId + " не ставил лайк фильму: " + film);
        }
        return film;
    }

    public List<Film> getTopFilmsByLikes(int count) {
        List<Film> films = new ArrayList<>(filmStorage.allFilms());

        if (count > films.size()) {
            count = films.size();
        }

        films = films.stream()
                .sorted(new FilmRatingComparator().reversed())
                .limit(count)
                .toList();

        return films;
    }

}
