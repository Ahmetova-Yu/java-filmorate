package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createFilm(@RequestBody Film film) {
        validate(film, "создании");
        Film createdFilm = filmService.createFilm(film);
        log.info("Фильм {} успешно создан", createdFilm.getId());
        return filmService.getFilmResponse(createdFilm);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("ID фильма не может быть null при обновлении");
            throw new ValidationException("ID фильма должен быть указан");
        }

        validate(newFilm, "обновлении");
        Film updatedFilm = filmService.updateFilm(newFilm);
        log.info("Фильм с id {} успешно обновлен", newFilm.getId());
        return filmService.getFilmResponse(updatedFilm);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> findAllFilms() {
        return filmService.findAllFilms().stream()
                .map(filmService::getFilmResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmById(id);
        return filmService.getFilmResponse(film);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> getMostPopularFilms(@RequestParam(defaultValue = "10")
                                                         @Positive(message = "Количество фильмов должно быть положительным") Integer count) {
        return filmService.getMostPopularFilms(count).stream()
                .map(filmService::getFilmResponse)
                .collect(Collectors.toList());
    }

    private void validate(Film film, String info) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации при {}: название фильма не должно быть пустым", info);
            throw new ValidationException("Имя фильма не должно быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации при {}: длина описания {} превышает 200 символов", info,
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.error("Ошибка валидации при {}: дата релиза не указана", info);
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.error("Ошибка валидации при {}: дата релиза {} раньше минимальной {}", info,
                    film.getReleaseDate(), minReleaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null) {
            log.error("Ошибка валидации при {}: продолжительность фильма не указана", info);
            throw new ValidationException("Продолжительность фильма должна быть указана");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации при {}: продолжительность фильма {} должна быть положительной", info, film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}