package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmLikes;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmLikesMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class FilmRepository {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;
    private final UserDbStorage userDbStorage;
    private final FilmLikesMapper filmLikesMapper;
    private final String QUERY_FOR_GET_FILM_BY_ID = "SELECT * FROM film WHERE film_id = ?;";
    private final String QUERY_FOR_ADD_NEW_LIKE = "INSERT INTO likes (film_id, user_id) " +
            "VALUES (?, ?);";
    private final String QUERY_FOR_GET_FILM_LIKES = "SELECT user_id FROM likes WHERE film_id = ?;";
    private final String QUERY_FOR_DELETE_LIKES = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
    private final String QUERY_FOR_GET_COUNT_LIKES = "SELECT film_id, COUNT(user_id) AS count_likes " +
            "FROM likes GROUP BY film_id;";

    @Autowired
    public FilmRepository(JdbcTemplate jdbcTemplate, FilmRowMapper mapper, UserDbStorage userDbStorage,
                          FilmLikesMapper filmLikesMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.userDbStorage = userDbStorage;
        this.filmLikesMapper = filmLikesMapper;
    }

    public Film putLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);
        Set<Long> likes = getFilmLikes(filmId);
        if (likes.contains(filmId)) {
            throw new ValidationException("Пользователь с ID = " + userId + " уже поставил лайк фильму с ID = " +
                    filmId);
        }
        int rowCount = jdbcTemplate.update(QUERY_FOR_ADD_NEW_LIKE, filmId, userId);
        if (rowCount == 0) {
            throw new ValidationException("Не удалось добавить лайк!");
        }
        film.getLikes().add(userId);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);
        Set<Long> likes = getFilmLikes(filmId);
        if (!likes.contains(userId)) {
            log.error("Пользователь с ID = {} не ставил лайк фильму: {}", userId, film);
            throw new ValidationException("Пользователь с ID = " + userId + " не ставил лайк фильму: " + film);
        }
        int rowCount = jdbcTemplate.update(QUERY_FOR_DELETE_LIKES, filmId, userId);
        if (rowCount == 0) {
            throw new ValidationException("Не удалось добавить лайк!");
        }
        film.getLikes().remove(userId);
        return film;
    }

    public List<Film> getTopFilmsByLikes(int count) {
        List<FilmLikes> filmLikes = jdbcTemplate.query(QUERY_FOR_GET_COUNT_LIKES, filmLikesMapper);
        if (count > filmLikes.size()) {
            count = filmLikes.size();
        }
        filmLikes = filmLikes.stream()
                .sorted(new FilmLikesComparator().reversed())
                .limit(count)
                .toList();
        return filmLikes.stream()
                .map(filmLikes1 -> getFilmById(filmLikes1.getFilmId()))
                .toList();
    }

    private Film getFilmById(Long id) {
        Film film = jdbcTemplate.queryForObject(QUERY_FOR_GET_FILM_BY_ID, mapper, id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        return film;
    }

    private Set<Long> getFilmLikes(Long film_id) {
        List<Long> getAllGenre = jdbcTemplate.queryForList(QUERY_FOR_GET_FILM_LIKES, Long.class, film_id);
        return new HashSet<>(getAllGenre);
    }
}
