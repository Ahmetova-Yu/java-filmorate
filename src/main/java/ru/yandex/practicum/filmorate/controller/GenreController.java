package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    private final JdbcTemplate jdbcTemplate;

    public GenreController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getAllGenres() {
        log.info("Запрос на получение всех жанров");

        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> genre = new HashMap<>();
            genre.put("id", rs.getInt("id"));
            genre.put("name", rs.getString("name"));
            return genre;
        });
    }

    @GetMapping("/{id}")
    public Map<String, Object> getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id {}", id);

        String sql = "SELECT id, name FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Object> genre = new HashMap<>();
                genre.put("id", rs.getInt("id"));
                genre.put("name", rs.getString("name"));
                return genre;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Жанр с id {} не найден", id);
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }
}