package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
@Qualifier("dbFilmStorage")
public class FilmDbStorage implements FilmStorage{
    private FilmRowMapper mapper;
    private JdbcTemplate jdbcTemplate;
    private final String QUERY_FOR_CREATE_FILMS = "INSERT INTO film (name, description, release_date, duration, " +
            "rating_id) VALUES (?, ?, ?, ?, ?);";
    private final String QUERY_FOR_GET_FILM_BY_ID = "SELECT * FROM film WHERE film_id = ?;";
    private final String QUERY_FOR_UPDATE_FILM = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?;";
    private final String QUERY_FOR_ADD_GENRE = "INSERT INTO film_genre (film_id, genre_id) " +
            "VALUES (?, ?)";
    private final String QUERY_FOR_DELETE_GENRE = "DELETE FROM film_genre WHERE film_id = ?";
    private final String QUERY_FOR_GET_FILM_GENRE = "SELECT genre_id FROM film_genre WHERE film_id = ?;";
    private final String QUERY_FOR_GET_ALL_GENRE = "SELECT genre_id FROM genre";
    private final String QUERY_FOR_GET_ALL_FILM = "SELECT * FROM film;";
    private final String QUERY_FOR_GET_ALL_RATING_ID = "SELECT rating_id FROM rating";
    private final String QUERY_FOR_GET_NAME_GENRE_BY_GENRE_ID = "SELECT name FROM genre WHERE genre_id = ?";

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
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                List<Integer> allRating = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_RATING_ID, Integer.class);
                if (!allRating.contains(film.getMpa().getId())) {
                    throw new NotFoundException("Рейтинга с ID = " +  film.getMpa().getId() + " не существует!");
                }
                ps.setInt(5, film.getMpa().getId());
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        film.setId(id);

        if (film.getGenres() != null) {
            List<Integer> allGenre = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_GENRE, Integer.class);
            List<GenreName> genreFromFilm = film.getGenres();
            List<Integer> addGenreIdCheck = new ArrayList<>();
            genreFromFilm.forEach(genre -> {
                if (!allGenre.contains(genre.getId())) {
                    throw new NotFoundException("Жанра с ID = " +  genre.getId() + " не существует!");
                }
                if (!addGenreIdCheck.contains(genre.getId())) {
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(QUERY_FOR_ADD_GENRE);
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genre.getId());
                        return ps;
                    });
                }
                addGenreIdCheck.add(genre.getId());
            });
        } else {
            film.setGenres(new ArrayList<>());
        }
        log.info("Создан фильм: {}", film);
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
        if (newFilm.getDescription() != null && !newFilm.getDescription().equals(film.getDescription())) {
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

        if (newFilm.getMpa() != null && newFilm.getMpa().getId() != (film.getMpa().getId())) {
            film.setMpa(newFilm.getMpa());
        }
        Integer rating_id;
        if (film.getMpa() != null) {
            rating_id = film.getMpa().getId();
        } else {
            rating_id = 5;
        }
        if (newFilm.getMpa() != null) {
            rating_id = newFilm.getMpa().getId();
        }

        int countRow = jdbcTemplate.update(QUERY_FOR_UPDATE_FILM, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), rating_id, film.getId());

        if (countRow == 0) {
            throw new ValidationException("Не удалось обновить данные");
        }

        if (film.getGenres() != null && newFilm.getGenres() != null) {
            List<Integer> genre = getFilmGenre(film.getId());
            genre.forEach(genre_id -> {
                jdbcTemplate.update(QUERY_FOR_DELETE_GENRE, genre_id);
            });
            newFilm.getGenres().forEach(genre_id -> {
                jdbcTemplate.update(QUERY_FOR_ADD_GENRE, film.getId(), genre_id);
            });
            film.setGenres(newFilm.getGenres());
        }
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @Override
    public Collection<Film> allFilms() {
        List<Film> allFilm = jdbcTemplate.query(QUERY_FOR_GET_ALL_FILM, mapper);
        allFilm.forEach(film -> film.setGenres(getGenreByFilm(film.getId())));
        return allFilm;
    }

    private List<GenreName> getGenreByFilm(Long film_id) {
        List<Integer> allGenre = getFilmGenre(film_id);
        return allGenre.stream()
                .map(genre_id -> {
                    GenreName genre = new GenreName();
                    genre.setId(genre_id);
                    String genreName = jdbcTemplate.queryForObject(QUERY_FOR_GET_NAME_GENRE_BY_GENRE_ID, String.class,
                            genre_id);
                    genre.setName(genreName);
                    return genre;
                })
                .toList();
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = jdbcTemplate.queryForObject(QUERY_FOR_GET_FILM_BY_ID, mapper, id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        if (getGenreByFilm(film.getId()).isEmpty()) {
            film.setGenres(new ArrayList<>());
        } else {
            film.setGenres(getGenreByFilm(film.getId()));
        }
        return film;
    }

    private List<Integer> getFilmGenre(Long film_id) {
        return jdbcTemplate.queryForList(QUERY_FOR_GET_FILM_GENRE, Integer.class, film_id);
    }

}
