package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        return Arrays.asList(
                new Mpa(1, "G"),
                new Mpa(2, "PG"),
                new Mpa(3, "PG-13"),
                new Mpa(4, "R"),
                new Mpa(5, "NC-17")
        );
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга MPA с id {}", id);

        switch (id) {
            case 1: return new Mpa(1, "G");
            case 2: return new Mpa(2, "PG");
            case 3: return new Mpa(3, "PG-13");
            case 4: return new Mpa(4, "R");
            case 5: return new Mpa(5, "NC-17");
            default: throw new NotFoundException("Рейтинг mpa с id " + id + " не найден");
        }
    }
}