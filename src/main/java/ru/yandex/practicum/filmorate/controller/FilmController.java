package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.*;
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
    public Map<String, Object> createFilm(@RequestBody Map<String, Object> filmData) {
        Film film = convertToFilm(filmData);
        validate(film, "создании");
        Film createdFilm = filmService.createFilm(film);
        log.info("Фильм {} успешно создан", createdFilm.getId());
        return convertToResponse(createdFilm);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> updateFilm(@RequestBody Map<String, Object> filmData) {
        if (!filmData.containsKey("id") || filmData.get("id") == null) {
            log.error("ID фильма не может быть null при обновлении");
            throw new ValidationException("ID фильма должен быть указан");
        }

        Film film = convertToFilm(filmData);
        validate(film, "обновлении");
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return convertToResponse(updatedFilm);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> findAllFilms() {
        return filmService.findAllFilms().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmById(id);
        return convertToResponse(film);
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
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Film convertToFilm(Map<String, Object> filmData) {
        Film film = new Film();

        if (filmData.containsKey("id")) {
            film.setId(Long.valueOf(filmData.get("id").toString()));
        }

        film.setName((String) filmData.get("name"));
        film.setDescription((String) filmData.get("description"));
        film.setReleaseDate(LocalDate.parse((String) filmData.get("releaseDate")));
        film.setDuration((Integer) filmData.get("duration"));

        // Обработка mpa
        if (filmData.containsKey("mpa") && filmData.get("mpa") != null) {
            Map<String, Object> mpaData = (Map<String, Object>) filmData.get("mpa");
            int mpaId = ((Integer) mpaData.get("id"));
            film.setMpaRating(MpaRating.values()[mpaId - 1]);
        }

        // Обработка жанров
        if (filmData.containsKey("genres") && filmData.get("genres") != null) {
            List<Map<String, Integer>> genresData = (List<Map<String, Integer>>) filmData.get("genres");
            Set<Genre> genres = new HashSet<>();
            for (Map<String, Integer> genreData : genresData) {
                int genreId = genreData.get("id");
                genres.add(Genre.values()[genreId - 1]);
            }
            film.setGenres(genres);
        }

        return film;
    }

    private Map<String, Object> convertToResponse(Film film) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", film.getId());
        response.put("name", film.getName());
        response.put("description", film.getDescription());
        response.put("releaseDate", film.getReleaseDate().toString());
        response.put("duration", film.getDuration());
        response.put("likes", new ArrayList<>(film.getLikes()));

        // Добавляем mpa объект
        if (film.getMpaRating() != null) {
            Map<String, Object> mpa = new HashMap<>();
            mpa.put("id", film.getMpaRating().ordinal() + 1);
            mpa.put("name", film.getMpaRating().name());
            response.put("mpa", mpa);
        } else {
            response.put("mpa", null);
        }

        // Добавляем жанры
        List<Map<String, Object>> genres = new ArrayList<>();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Map<String, Object> genreMap = new HashMap<>();
                genreMap.put("id", genre.ordinal() + 1);
                genreMap.put("name", genre.getName());
                genres.add(genreMap);
            }
        }
        // Сортируем жанры по id
        genres.sort(Comparator.comparingInt(g -> (int) g.get("id")));
        response.put("genres", genres);

        return response;
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