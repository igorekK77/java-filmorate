package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmLikes;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmLikesMapper implements RowMapper<FilmLikes> {
    @Override
    public FilmLikes mapRow(ResultSet resultSet, int rowCount) throws SQLException {
        FilmLikes filmLikes = new FilmLikes();
        filmLikes.setFilmId(resultSet.getLong("film_id"));
        filmLikes.setCountLikes(resultSet.getInt("count_likes"));
        return filmLikes;
    }
}
