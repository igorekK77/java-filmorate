package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriends;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserRowMapper implements RowMapper<User> {
    private final JdbcTemplate jdbcTemplate;
    private final UserFriendsMapper userFriendsMapper;

    @Autowired
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
        Timestamp timestamp = resultSet.getTimestamp("birthday");
        user.setBirthday(timestamp.toLocalDateTime().toLocalDate());

        String queryForUserFriends = "SELECT friend_id, status FROM user_friends WHERE user_id = ?";
        Map<Long, String> friends = new HashMap<>();
        List<UserFriends> userFriends = jdbcTemplate.query(queryForUserFriends, userFriendsMapper, user.getId());
        userFriends.forEach(userFriend-> friends.put(userFriend.getId(), userFriend.getStatus()));
        user.setFriends(friends);
        return user;
    }
}
