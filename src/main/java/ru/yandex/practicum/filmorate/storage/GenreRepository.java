package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Repository
public class GenreRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String queryForGetGenre = "SELECT name FROM genre";
    private final String queryForGetGenreById = "SELECT name FROM genre WHERE genre_id = ?;";
    private final String queryForGetAllGenreId = "SELECT genre_id FROM genre";

    @Autowired
    public GenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GenreName> allGenre() {
       List<String> getAllGenre = jdbcTemplate.queryForList(queryForGetGenre, String.class);
       return getAllGenre.stream()
               .map(genre -> new GenreName(getAllGenre.indexOf(genre) + 1, genre))
               .toList();
    }

    public GenreName getGenreById(int id) {
        List<Integer> allGenreId = jdbcTemplate.queryForList(queryForGetAllGenreId, Integer.class);
        if (!allGenreId.contains(id)) {
            throw new NotFoundException("Жанра с таким ID не существует!");
        }
        String genre = jdbcTemplate.queryForObject(queryForGetGenreById, String.class, id);
        return new GenreName(id, genre);
    }
}
