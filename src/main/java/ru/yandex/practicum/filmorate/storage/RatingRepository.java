package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Repository
public class RatingRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String QUERY_FOR_GET_ALL_RATING = "SELECT name FROM rating";
    private final String QUERY_FOR_GET_RATING_BY_RATING_ID = "SELECT name FROM rating WHERE rating_id = ?";
    private final String QUERY_FOR_GET_ALL_RATING_ID = "SELECT rating_id FROM rating";

    @Autowired
    public RatingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RatingName> allRating() {
        List<String> allRating = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_RATING, String.class);
        return allRating.stream()
                .map(rating -> new RatingName(allRating.indexOf(rating) + 1, rating))
                .toList();
    }

    public RatingName getRatingById(int rating_id) {
        List<Integer> allRatingId = jdbcTemplate.queryForList(QUERY_FOR_GET_ALL_RATING_ID, Integer.class);
        if (!allRatingId.contains(rating_id)) {
            throw new NotFoundException("Рейтинга с ID = " + rating_id + " не существует!");
        }
        String rating = jdbcTemplate.queryForObject(QUERY_FOR_GET_RATING_BY_RATING_ID, String.class, rating_id);
        return new RatingName(rating_id, rating);
    }

}
