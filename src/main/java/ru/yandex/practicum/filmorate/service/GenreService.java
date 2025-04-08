package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.storage.GenreRepository;

import java.util.List;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<GenreName> allGenre() {
        return genreRepository.allGenre();
    }

    public GenreName getGenreById(int id) {
        return genreRepository.getGenreById(id);
    }
}
