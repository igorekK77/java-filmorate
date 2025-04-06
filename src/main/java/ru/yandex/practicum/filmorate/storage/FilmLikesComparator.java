package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;

public class FilmLikesComparator implements Comparator<Film> {
    @Override
    public int compare(Film filmLikes1, Film filmLikes2) {
        return filmLikes1.getLikes().size() - filmLikes2.getLikes().size();
    }
}
