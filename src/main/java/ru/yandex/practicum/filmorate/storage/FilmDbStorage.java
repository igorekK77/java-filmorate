package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
@Qualifier("dbFilmStorage")
public class FilmDbStorage implements FilmStorage{
    private FilmRowMapper mapper;
    private JdbcTemplate jdbcTemplate;
    private final String QUERY_FOR_CREATE_FILMS = "INSERT INTO film (name, description, release_date, duration, " +
            "rating) VALUES (?, ?, ?, ?, ?);";
    private final String QUERY_FOR_GET_FILM_BY_ID = "SELECT * FROM film WHERE film_id = ?;";
    private final String QUERY_FOR_UPDATE_FILM = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, rating = ? WHERE film_id = ?;";
    private final String QUERY_FOR_ADD_GENRE = "INSERT INTO film_genre (film_id, genre_id) " +
            "VALUES (?, ?)";
    private final String QUERY_FOR_DELETE_GENRE = "DELETE FROM film_genre WHERE film_id = ?";
    private final String QUERY_FOR_GET_FILM_GENRE = "SELECT genre_id FROM film_genre WHERE film_id = ?;";
    private final String QUERY_FOR_GET_FILM_LIKES = "SELECT user_id FROM likes WHERE film_id = ?;";
    private final String QUERY_FOR_GET_ALL_FILM = "SELECT * FROM film;";

    @Autowired
    public FilmDbStorage(FilmRowMapper mapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Фильм с ID = {}. Название фильма не может быть пустым", film.getId());
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.error("Фильм с ID = {}. Максимальная длина описания — 200 символов", film.getId());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null ||
                !film.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
            log.error("Фильм с ID = {}. Дата релиза — не раньше 28 декабря 1895 года", film.getId());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() < 0) {
            log.error("Фильм с ID = {}. Продолжительность фильма должна быть положительным числом.", film.getId());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(QUERY_FOR_CREATE_FILMS, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            return ps;
        }, keyHolder);

        Set<Long> genre = film.getGenre();
        if (!genre.isEmpty()) {
            genre.forEach(genreId -> {
                jdbcTemplate.update(connection -> {
                    PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FOR_ADD_GENRE);
                    preparedStatement.setLong(1, film.getId());
                    preparedStatement.setLong(2, genreId);
                    return preparedStatement;
                });
            });

        }
        Long id = keyHolder.getKeyAs(Long.class);

        film.setId(id);

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        Film film = getFilmById(newFilm.getId());

        if (newFilm.getName() != null && !newFilm.getName().equals(film.getName())) {
            film.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().equals(film.getDescription())) {
            if (newFilm.getDescription().length() > 200) {
                log.error("Максимальная длина описания — 200 символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            film.setDescription(newFilm.getDescription());
            log.debug("Новое описание фильма с id {}: {}", film.getId(), film.getDescription());
        }
        if (newFilm.getDuration() != null && !newFilm.getDuration().equals(film.getDuration())) {
            film.setDuration(newFilm.getDuration());
        }
        if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(film.getReleaseDate())) {
            if (!newFilm.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
                log.error("Дата релиза — не раньше 28 декабря 1895 года");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }
            film.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getRating() != null && !newFilm.getRating().equals(film.getRating())) {
            film.setRating(newFilm.getRating());
        }

        int countRow = jdbcTemplate.update(QUERY_FOR_UPDATE_FILM, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getRating(), film.getId());

        if (countRow == 0) {
            throw new ValidationException("Не удалось обновить данные");
        }

        if (!film.getGenre().isEmpty()) {
            Set<Long> genre = getFilmGenre(film.getId());
            genre.forEach(genre_id -> {
                jdbcTemplate.update(QUERY_FOR_DELETE_GENRE, genre_id);
            });
            newFilm.getGenre().forEach(genre_id -> {
                jdbcTemplate.update(QUERY_FOR_ADD_GENRE, film.getId(), genre_id);
            });
            film.setGenre(newFilm.getGenre());
        }

        return film;
    }

    @Override
    public Collection<Film> allFilms() {
        return jdbcTemplate.query(QUERY_FOR_GET_ALL_FILM, mapper);
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = jdbcTemplate.queryForObject(QUERY_FOR_GET_FILM_BY_ID, mapper, id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        return film;
    }

    private Set<Long> getFilmGenre(Long film_id) {
        List<Long> getAllGenre = jdbcTemplate.queryForList(QUERY_FOR_GET_FILM_GENRE, Long.class, film_id);
        return new HashSet<Long>(getAllGenre);
    }

    private Set<Long> getFilmLikes(Long film_id) {
        List<Long> getAllGenre = jdbcTemplate.queryForList(QUERY_FOR_GET_FILM_LIKES, Long.class, film_id);
        return new HashSet<>(getAllGenre);
    }
}
