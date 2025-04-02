package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private Long id;

    @NotNull
    private String email;

    @NotBlank
    private String login;

    private String name;

    private LocalDate birthday;

    private Map<Long, String> friends; //создать dto для получение объекта с полями id и статус

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        friends = new HashMap<>();
    }

    public User() {

    }
}
