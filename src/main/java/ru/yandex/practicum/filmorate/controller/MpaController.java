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
@RequestMapping("/mpa")
public class MpaController {

    private final JdbcTemplate jdbcTemplate;

    public MpaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> mpa = new HashMap<>();
            mpa.put("id", rs.getInt("id"));
            mpa.put("name", rs.getString("name"));
            return mpa;
        });
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMpaById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга MPA с id {}", id);
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Object> mpa = new HashMap<>();
                mpa.put("id", rs.getInt("id"));
                mpa.put("name", rs.getString("name"));
                return mpa;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Рейтинг MPA с id {} не найден", id);
            throw new NotFoundException("Рейтинг mpa с id " + id + " не найден");
        }
    }
}