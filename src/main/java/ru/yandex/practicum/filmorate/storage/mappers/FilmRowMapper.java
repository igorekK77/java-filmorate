package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.model.Film;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final JdbcTemplate jdbcTemplate;
    private final GenreNameMapper genreNameMapper;
    private final String queryForGetNameRatingByRatingId = "SELECT name FROM rating WHERE rating_id = ?";

    public FilmRowMapper(JdbcTemplate jdbcTemplate, GenreNameMapper genreNameMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreNameMapper = genreNameMapper;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowCount) throws SQLException {
        Film film = new Film();
        RatingName rating = new RatingName();
        rating.setId(resultSet.getInt("rating_id"));
        String ratingName = jdbcTemplate.queryForObject(queryForGetNameRatingByRatingId, String.class,
                rating.getId());
        rating.setName(ratingName);
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getInt("duration"));
        film.setMpa(rating);

        List<GenreName> allGenre = jdbcTemplate.query("SELECT fg.genre_id, g.name  FROM FILM_GENRE fg \n" +
                "JOIN GENRE g ON fg.GENRE_ID = g.GENRE_ID\n" +
                "WHERE fg.FILM_ID = ?", genreNameMapper, film.getId());
        film.setGenres(allGenre);

        Set<Long> usersLikes = new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes " +
                        "WHERE film_id = ?", Long.class, film.getId()));

        film.setLikes(usersLikes);

        Timestamp timestamp = resultSet.getTimestamp("release_date");
        film.setReleaseDate(timestamp.toLocalDateTime().toLocalDate());

        return film;
    }
}
