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
    public List<Map<String, Object>> getAllGenres() {
        log.info("Запрос на получение всех жанров");

        List<Map<String, Object>> genres = new ArrayList<>();
        Genre[] enumValues = Genre.values();

        for (int i = 0; i < enumValues.length; i++) {
            Map<String, Object> genre = new HashMap<>();
            genre.put("id", i + 1);
            genre.put("name", enumValues[i].name());
            genres.add(genre);
        }

        return genres;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id {}", id);

        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }

        Map<String, Object> genre = new HashMap<>();
        genre.put("id", id);
        genre.put("name", Genre.values()[id - 1].name());

        return genre;
    }
}