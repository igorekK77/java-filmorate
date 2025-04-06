package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreName;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreNameMapper implements RowMapper<GenreName> {
    @Override
    public GenreName mapRow(ResultSet resultSet, int rowCount) throws SQLException {
        GenreName genreName = new GenreName();
        genreName.setId(resultSet.getInt("genre_id"));
        genreName.setName(resultSet.getString("name"));
        return genreName;
    }
}
