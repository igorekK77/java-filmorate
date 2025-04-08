import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dto.RatingName;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({RatingRepository.class})
public class MpaApplicationTests {
    private final RatingRepository ratingRepository;


    @Test
    public void testGetAllMpa() {
        List<RatingName> ratings = List.of(
                new RatingName(1, "G"),
                new RatingName(2, "PG"),
                new RatingName(3, "PG-13"),
                new RatingName(4, "R"),
                new RatingName(5, "NC-17")
        );

        Assertions.assertEquals(ratings, ratingRepository.allRating());
    }

    @Test
    public void testGetMpaById() {
        RatingName ratingNc = new RatingName(5, "NC-17");
        Assertions.assertEquals(ratingNc, ratingRepository.getRatingById(5));
    }
}
