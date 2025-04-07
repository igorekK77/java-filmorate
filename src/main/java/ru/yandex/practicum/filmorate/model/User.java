package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserFriends;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class User {
    private Long id;

    @NotNull
    private String email;

    @NotBlank
    private String login;

    private String name;

    private LocalDate birthday;

    private List<UserFriends> friends;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}
