package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film createFilm(@RequestBody Film film) {

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());

        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setDescription(newFilm.getDescription());
            return oldFilm;
        }

        return null;
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
