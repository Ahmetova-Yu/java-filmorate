package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    @GetMapping
    public List<Map<String, Object>> getAllGenres() {
        log.info("Запрос на получение всех жанров");

        return Arrays.stream(Genre.values())
                .map(genre -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", genre.ordinal() + 1);
                    map.put("name", genre.getName());
                    return map;
                })
                .sorted(Comparator.comparingInt(m -> (int) m.get("id")))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id {}", id);

        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }

        Genre genre = Genre.values()[id - 1];
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", genre.getName());

        return response;
    }
}