package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriends;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserFriendsMapper implements RowMapper<UserFriends> {
    @Override
    public UserFriends mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        UserFriends userFriends = new UserFriends();
        userFriends.setId(resultSet.getLong("friend_id"));
        userFriends.setStatus(resultSet.getString("status"));
        return userFriends;
    }
}
