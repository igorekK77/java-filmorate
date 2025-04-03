package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.GenreName;
import ru.yandex.practicum.filmorate.dto.RatingName;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
public class Film {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    private String description;

    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    private Set<Long> likes;

    private List<GenreName> genres;

    private RatingName mpa;

    public Film(String name, String description, LocalDate releaseDate, int duration, List<GenreName> genres,
                RatingName mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        likes = new HashSet<>();
        this.mpa = mpa;
        this.genres = genres;
    }

}



