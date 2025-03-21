package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film putLike(Long filmId, Long userId) {
        Film film = checkFilmAndUser(filmId, userId);

        film.getLikes().add(userId);
        log.info("Пользователь с ID = {} поставил лайк фильму: {}", userId, film);
        filmStorage.update(film);

        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = checkFilmAndUser(filmId, userId);

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.info("Пользователь с ID = {} удалил лайк фильму: {}", userId, film);
            filmStorage.update(film);
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

        Set<Film> totalSetFilms = new LinkedHashSet<>();
        while (totalSetFilms.size() <= count) {
            int maxLikes = -1;
            Film totalFilm = null;
            for (Film film: films) {
                if (film.getLikes().size() > maxLikes) {
                    maxLikes = film.getLikes().size();
                    totalFilm = film;
                }
            }
            totalSetFilms.add(totalFilm);
            films.remove(totalFilm);
        }
        List<Film> totalListFilm = new ArrayList<>(totalSetFilms);
        Collections.reverse(totalListFilm);

        return totalListFilm;
    }

    private Film checkFilmAndUser(Long filmId, Long userId) {
        Film film;

        Optional<User> userOptional = userStorage.allUser().stream()
                .filter(user1 -> user1.getId().equals(userId))
                .findFirst();

        Optional<Film> filmOptional = filmStorage.allFilms().stream()
                .filter(film1 -> film1.getId().equals(filmId))
                .findFirst();

        if (userOptional.isEmpty()) {
            log.error("Пользователя с Id = {} не существует", userId);
            throw new NotFoundException("Пользователя с ID = " + userId + " не существует!");
        }

        if (filmOptional.isEmpty()) {
            log.error("Фильма с Id = {} не существует", filmId);
            throw new NotFoundException("Фильма с ID = " + filmId + " не существует!");
        } else {
            film = filmOptional.get();
        }
        return film;
    }

}
