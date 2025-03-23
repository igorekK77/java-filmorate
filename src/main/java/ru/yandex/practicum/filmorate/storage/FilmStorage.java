package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

public interface FilmStorage {
    public Film create(Film film);

    public Film update(Film newFilm);

    public Collection<Film> allFilms();

    public Map<Long, Film> getFilms();
}
