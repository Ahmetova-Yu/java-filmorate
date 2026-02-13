package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@RequestBody User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не должна быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }

        user.setId(getNextId());

        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            return oldUser;
        }

        return null;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
