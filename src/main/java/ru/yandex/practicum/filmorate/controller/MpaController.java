package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<Map<String, Object>> getAllMpa() {
        log.info("Запрос на получение всех рейтингов mpa");

        List<Map<String, Object>> ratings = new ArrayList<>();
        MpaRating[] enumValues = MpaRating.values();

        for (int i = 0; i < enumValues.length; i++) {
            Map<String, Object> rating = new HashMap<>();
            rating.put("id", i + 1);
            rating.put("name", enumValues[i].name());
            ratings.add(rating);
        }

        return ratings;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга mpa с id {}", id);

        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("Рейтинг mpa с id " + id + " не найден");
        }

        Map<String, Object> rating = new HashMap<>();
        rating.put("id", id);
        rating.put("name", MpaRating.values()[id - 1].name());

        return rating;
    }
}