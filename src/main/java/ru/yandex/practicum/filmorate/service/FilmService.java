package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikesRepository;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final LikesRepository likesRepository;

    @Autowired
    public FilmService(@Qualifier("dbFilmStorage") FilmStorage filmStorage, LikesRepository likesRepository) {
        this.filmStorage = filmStorage;
        this.likesRepository = likesRepository;
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

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film putLike(Long filmId, Long userId) {
        return likesRepository.putLike(filmId, userId);
    }

    public Film deleteLike(Long filmId, Long userId) {
        return likesRepository.deleteLike(filmId, userId);
    }

    public List<Film> getTopFilmsByLikes(int count) {
        return likesRepository.getTopFilmsByLikes(count);
    }

}
