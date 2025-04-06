package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Component
public class UserRowMapper implements RowMapper<User> {

    private final JdbcTemplate jdbcTemplate;
    private final UserFriendsMapper userFriendsMapper;

    public UserRowMapper(JdbcTemplate jdbcTemplate, UserFriendsMapper userFriendsMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userFriendsMapper = userFriendsMapper;
    }

    @Override
    public User mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));

        Timestamp timestamp = resultSet.getTimestamp("birthday");
        user.setBirthday(timestamp.toLocalDateTime().toLocalDate());

        List<UserFriends> allFriends = jdbcTemplate.query("SELECT uf.friend_id, uf.status\n" +
                "FROM user_friends uf JOIN users u\n" +
                "ON uf.user_id = u.user_id\n" +
                "WHERE u.user_id = ?;", userFriendsMapper, user.getId());
        user.setFriends(allFriends);

        return user;
    }
}
