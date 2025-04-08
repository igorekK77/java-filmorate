import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;

@ContextConfiguration(classes = FilmorateApplication.class)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreRepository.class})
public class GenreApplicationTests {
    private final GenreRepository genreRepository;

    @Test
    public void testGetAllGenre() {
        List<GenreName> genres = List.of(
                new GenreName(1, "Комедия"),
                new GenreName(2, "Драма"),
                new GenreName(3, "Мультфильм"),
                new GenreName(4, "Триллер"),
                new GenreName(5, "Документальный"),
                new GenreName(6, "Боевик")
        );

        Assertions.assertEquals(genres, genreRepository.allGenre());
    }

    @Test
    public void testGetGenreById() {
        GenreName thrillerGenre = new GenreName(4, "Триллер");
        Assertions.assertEquals(thrillerGenre, genreRepository.getGenreById(4));
    }
}
