package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.model.Film;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowCount) throws SQLException {
        Film film = new Film();
        RatingName rating = new RatingName();
        rating.setId(resultSet.getInt("rating_id"));
        rating.setName(resultSet.getString("rating_name"));
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getInt("duration"));
        film.setMpa(rating);

        Set<Long> usersLikes = new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes " +
                        "WHERE film_id = ?", Long.class, film.getId()));

        film.setLikes(usersLikes);

        Timestamp timestamp = resultSet.getTimestamp("release_date");
        film.setReleaseDate(timestamp.toLocalDateTime().toLocalDate());

        return film;
    }
}
