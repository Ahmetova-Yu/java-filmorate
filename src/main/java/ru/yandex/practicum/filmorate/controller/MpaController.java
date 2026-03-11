package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<Map<String, Object>> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");

        return Arrays.stream(MpaRating.values())
                .map(mpa -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", mpa.ordinal() + 1);
                    map.put("name", mpa.name());
                    return map;
                })
                .sorted(Comparator.comparingInt(m -> (int) m.get("id")))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга MPA с id {}", id);

        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("Рейтинг mpa с id " + id + " не найден");
        }

        MpaRating mpa = MpaRating.values()[id - 1];
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", mpa.name());

        return response;
    }
}