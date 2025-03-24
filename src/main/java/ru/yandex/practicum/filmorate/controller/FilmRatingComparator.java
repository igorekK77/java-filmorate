package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;

public class FilmRatingComparator implements Comparator<Film> {
    @Override
    public int compare(Film film1, Film film2) {
        return film1.getLikes().size() - film2.getLikes().size();
    }
}
