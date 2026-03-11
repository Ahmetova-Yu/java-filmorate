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

    private final Map<Integer, String> mpaMap = new LinkedHashMap<>();

    public MpaController() {
        mpaMap.put(1, "G");
        mpaMap.put(2, "PG");
        mpaMap.put(3, "PG-13");
        mpaMap.put(4, "R");
        mpaMap.put(5, "NC-17");
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        List<Mpa> ratings = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : mpaMap.entrySet()) {
            Mpa mpa = new Mpa();
            mpa.setId(entry.getKey());
            mpa.setName(entry.getValue());
            ratings.add(mpa);
        }
        return ratings;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга MPA с id {}", id);
        if (!mpaMap.containsKey(id)) {
            throw new NotFoundException("Рейтинг mpa с id " + id + " не найден");
        }
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(mpaMap.get(id));
        return mpa;
    }
}