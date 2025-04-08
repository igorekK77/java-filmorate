package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmGenreName;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmGenreMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreNameMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
@Qualifier("dbFilmStorage")
public class FilmRepository implements FilmStorage {
    private FilmRowMapper mapper;
    private JdbcTemplate jdbcTemplate;
    private GenreNameMapper genreNameMapper;
    private FilmGenreMapper filmGenreMapper;
    private final String queryForCreateFilms = "INSERT INTO film (name, description, release_date, duration, " +
            "rating_id) VALUES (?, ?, ?, ?, ?);";
    private final String queryForGetFilmById = "SELECT f.*, r.name AS rating_name " +
            "FROM film AS f JOIN rating AS r " +
            "ON f.rating_id = r.rating_id " +
            "WHERE film_id = ?;";
    private final String queryForUpdateFilm = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?;";
    private final String queryForAddGenre = "INSERT INTO film_genre (film_id, genre_id) " +
            "VALUES (?, ?)";
    private final String queryForDeleteGenre = "DELETE FROM film_genre WHERE film_id = ?";
    private final String queryForGetFilmGenre = "SELECT genre_id FROM film_genre WHERE film_id = ?;";
    private final String queryForGetAllGenre = "SELECT genre_id FROM genre";
    private final String queryForGetAllFilm = "SELECT f.*, r.name AS rating_name " +
            "FROM film AS f JOIN rating AS r " +
            "ON f.rating_id = r.rating_id;";
    private final String queryForGetAllRatingId = "SELECT rating_id FROM rating";

    @Autowired
    public FilmRepository(FilmRowMapper mapper, JdbcTemplate jdbcTemplate, GenreNameMapper genreNameMapper,
                          FilmGenreMapper filmGenreMapper) {
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
        this.genreNameMapper = genreNameMapper;
        this.filmGenreMapper = filmGenreMapper;
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
            PreparedStatement ps = connection.prepareStatement(queryForCreateFilms, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                List<Integer> allRating = jdbcTemplate.queryForList(queryForGetAllRatingId, Integer.class);
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
            List<Integer> allGenre = jdbcTemplate.queryForList(queryForGetAllGenre, Integer.class);
            Set<GenreName> allGenreFromFilm = new HashSet<>(film.getGenres());
            List<GenreName> genreFromFilm = new ArrayList<>(allGenreFromFilm);


            jdbcTemplate.batchUpdate(queryForAddGenre, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    if (!allGenre.contains(genreFromFilm.get(i).getId())) {
                        throw new NotFoundException("Жанра с ID = " + genreFromFilm.get(i).getId() + " не существует!");
                    }
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genreFromFilm.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return genreFromFilm.size();
                }
            });

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
        Integer ratingId;
        if (film.getMpa() != null) {
            ratingId = film.getMpa().getId();
        } else {
            ratingId = 5;
        }
        if (newFilm.getMpa() != null) {
            ratingId = newFilm.getMpa().getId();
        }

        int countRow = jdbcTemplate.update(queryForUpdateFilm, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), ratingId, film.getId());

        if (countRow == 0) {
            throw new ValidationException("Не удалось обновить данные");
        }

        if (film.getGenres() != null && newFilm.getGenres() != null) {
            List<Integer> genre = getFilmGenre(film.getId());
            jdbcTemplate.batchUpdate(queryForDeleteGenre, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, film.getId());
                }

                @Override
                public int getBatchSize() {
                    return genre.size();
                }
            });
            jdbcTemplate.batchUpdate(queryForAddGenre, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, newFilm.getGenres().get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return newFilm.getGenres().size();
                }
            });
            film.setGenres(newFilm.getGenres());
        }
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @Override
    public Collection<Film> allFilms() {
        List<Film> allFilms = jdbcTemplate.query(queryForGetAllFilm, mapper);
        String query = "SELECT fg.film_id, fg.genre_id, g.name FROM film_genre AS fg JOIN genre AS g " +
                "ON fg.genre_id = g.genre_id ORDER BY film_id, genre_id";
        List<FilmGenreName> allGenre = jdbcTemplate.query(query, filmGenreMapper);
        Map<Long, List<GenreName>> filmGenre = getGenreNameFilm(allGenre);
        allFilms.forEach(film -> film.setGenres(filmGenre.get(film.getId())));
        return allFilms;
    }

    private Map<Long, List<GenreName>> getGenreNameFilm(List<FilmGenreName> allGenre) {
        Map<Long, List<GenreName>> genreMap = new HashMap<>();
        for (FilmGenreName filmGenre: allGenre) {
            Long filmId = filmGenre.getFilmId();
            GenreName genreName = new GenreName(filmGenre.getGenreId(), filmGenre.getName());

            if (genreMap.containsKey(filmId)) {
                genreMap.get(filmId).add(genreName);
            } else {
                genreMap.put(filmId, new ArrayList<>());
                genreMap.get(filmId).add(genreName);
            }
        }
        return genreMap;
    }

    private List<GenreName> getGenreByFilm(Long filmId) {
        String query = "SELECT g.genre_id, g.name FROM genre AS g JOIN " +
                "film_genre AS fg ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbcTemplate.query(query, genreNameMapper, filmId);
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = jdbcTemplate.queryForObject(queryForGetFilmById, mapper, id);
        if (film == null) {
            log.error("Фильма с Id = {} не существует", id);
            throw new NotFoundException("Фильма с ID = " + id + " не существует!");
        }
        List<GenreName> allGenre = getGenreByFilm(film.getId());
        if (!allGenre.isEmpty()) {
            film.setGenres(allGenre);
        }
        return film;
    }

    private List<Integer> getFilmGenre(Long filmId) {
        return jdbcTemplate.queryForList(queryForGetFilmGenre, Integer.class, filmId);
    }

}
