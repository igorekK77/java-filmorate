package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.FilmLikes;

import java.util.Comparator;

public class FilmLikesComparator implements Comparator<FilmLikes> {
    @Override
    public int compare(FilmLikes filmLikes1, FilmLikes filmLikes2) {
        return filmLikes1.getCountLikes() - filmLikes2.getCountLikes();
    }
}
