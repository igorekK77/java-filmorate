package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    public Film create(Film film);

    public Film update(Film newFilm);

    public Collection<Film> allFilms();

    public Film getFilmById(Long id);
}
