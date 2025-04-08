package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));

        Timestamp timestamp = resultSet.getTimestamp("birthday");
        user.setBirthday(timestamp.toLocalDateTime().toLocalDate());

        return user;
    }
}
