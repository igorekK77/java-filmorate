package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmGenreName;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenreMapper implements RowMapper<FilmGenreName> {
    @Override
    public FilmGenreName mapRow(ResultSet resultSet, int rowCount) throws SQLException {
        return new FilmGenreName(
                resultSet.getLong("film_id"),
                resultSet.getInt("genre_id"),
                resultSet.getString("name")
        );
    }
}
