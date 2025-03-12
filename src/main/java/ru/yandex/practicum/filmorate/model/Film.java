package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.yandex.practicum.filmorate.validate.Marker;

import java.time.LocalDate;


@Data
public class Film {

    @NotNull
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    private String description;

    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

}
