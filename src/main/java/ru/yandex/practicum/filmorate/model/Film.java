package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
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

    private Set<Long> genre;

    private String rating;

    public Film(String name, String description, LocalDate releaseDate, int duration, Set<Long> genre,
                String rating) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        likes = new HashSet<>();
        this.rating = rating;
        this.genre = genre;
    }

}