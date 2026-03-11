package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        return Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")
        );
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id {}", id);

        switch (id) {
            case 1: return new Genre(1, "Комедия");
            case 2: return new Genre(2, "Драма");
            case 3: return new Genre(3, "Мультфильм");
            case 4: return new Genre(4, "Триллер");
            case 5: return new Genre(5, "Документальный");
            case 6: return new Genre(6, "Боевик");
            default: throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }
}