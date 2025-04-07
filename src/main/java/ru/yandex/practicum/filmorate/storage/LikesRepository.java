package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class LikesRepository {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;
    private final UserRepository userRepository;
    private final String queryForGetFilmById = "SELECT f.*, r.name AS rating_name " +
            "FROM film AS f JOIN rating AS r " +
            "ON f.rating_id = r.rating_id " +
            "WHERE film_id = ?;";
    private final String queryForAddNewLike = "INSERT INTO likes (film_id, user_id) " +
            "VALUES (?, ?);";
    private final String queryForDeleteLikes = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

    @Autowired
    public LikesRepository(JdbcTemplate jdbcTemplate, FilmRowMapper mapper, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public Film putLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = userRepository.getUserById(userId);
        int rowCount = jdbcTemplate.update(queryForAddNewLike, filmId, userId);
        if (rowCount == 0) {
            throw new ValidationException("Пользователь с ID = " + userId + " уже поставил лайк фильму с ID = " +
                    filmId);
        }
        if (film.getLikes() == null) {
            film.setLikes(Set.of(userId));
        } else {
            film.getLikes().add(userId);
        }
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        Set<Long> allLikes = new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?",
                Long.class, film.getId()));
        film.setLikes(allLikes);
        User user = userRepository.getUserById(userId);
        int rowCount = jdbcTemplate.update(queryForDeleteLikes, filmId, userId);
        if (rowCount == 0) {
            log.error("Пользователь с ID = {} не ставил лайк фильму: {}", userId, film);
            throw new ValidationException("Пользователь с ID = " + userId + " не ставил лайк фильму: " + film);
        }
        if (!allLikes.isEmpty()) {
            film.getLikes().remove(userId);
        }
        return film;
    }

    public List<Film> getTopFilmsByLikes(int count) {
        String query = "SELECT f.*, r.name AS rating_name " +
                "FROM film AS f JOIN rating AS r " +
                "ON f.rating_id = r.rating_id " +
                "JOIN (" +
                "    SELECT film_id " +
                "    FROM likes " +
                "    GROUP BY film_id " +
                "    ORDER BY COUNT(user_id) DESC " +
                "    LIMIT " + count + " " +
                ") AS top_films_by_like ON f.film_id = top_films_by_like.film_id;";

        return jdbcTemplate.query(query, mapper);
    }

    private Film getFilmById(Long id) {
        Film film = jdbcTemplate.queryForObject(queryForGetFilmById, mapper, id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        return film;
    }
}
