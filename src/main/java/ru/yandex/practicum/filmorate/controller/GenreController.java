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

    private final Map<Integer, String> genreMap = new LinkedHashMap<>();

    public GenreController() {
        genreMap.put(1, "Комедия");
        genreMap.put(2, "Драма");
        genreMap.put(3, "Мультфильм");
        genreMap.put(4, "Триллер");
        genreMap.put(5, "Документальный");
        genreMap.put(6, "Боевик");
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        List<Genre> genres = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : genreMap.entrySet()) {
            Genre genre = new Genre();
            genre.setId(entry.getKey());
            genre.setName(entry.getValue());
            genres.add(genre);
        }
        return genres;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id {}", id);
        if (!genreMap.containsKey(id)) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(genreMap.get(id));
        return genre;
    }
}