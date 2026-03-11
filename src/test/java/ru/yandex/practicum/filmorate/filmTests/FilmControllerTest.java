package ru.yandex.practicum.filmorate.filmTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private Film validFilm;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);

        validFilm = new Film();
        validFilm.setName("Тестовый фильм");
        validFilm.setDescription("Описание");
        validFilm.setReleaseDate(LocalDate.of(2020, 1, 4));
        validFilm.setDuration(120);
        validFilm.setMpaRating(MpaRating.PG_13);
        validFilm.setGenres(Set.of(Genre.COMEDY, Genre.ACTION));
    }

    @Test
    void createFilm_ShouldThrowException_WhenNameIsEmpty() {
        validFilm.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(Map.of(
                        "name", "",
                        "description", validFilm.getDescription(),
                        "releaseDate", validFilm.getReleaseDate().toString(),
                        "duration", validFilm.getDuration(),
                        "mpa", Map.of("id", 3)
                )));
        assertEquals("Имя фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenNameIsNull() {
        Map<String, Object> filmData = new HashMap<>();
        filmData.put("name", null);
        filmData.put("description", validFilm.getDescription());
        filmData.put("releaseDate", validFilm.getReleaseDate().toString());
        filmData.put("duration", validFilm.getDuration());
        filmData.put("mpa", Map.of("id", 3));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmData));
        assertEquals("Имя фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDescriptionIsTooLong() {
        String longDescription = "a".repeat(201);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(Map.of(
                        "name", validFilm.getName(),
                        "description", longDescription,
                        "releaseDate", validFilm.getReleaseDate().toString(),
                        "duration", validFilm.getDuration(),
                        "mpa", Map.of("id", 3)
                )));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenDescriptionIsExactly200Chars() {
        String exactDescription = "a".repeat(200);

        Map<String, Object> createdFilm = filmController.createFilm(Map.of(
                "name", validFilm.getName(),
                "description", exactDescription,
                "releaseDate", validFilm.getReleaseDate().toString(),
                "duration", validFilm.getDuration(),
                "mpa", Map.of("id", 3)
        ));

        assertNotNull(createdFilm.get("id"));
        assertEquals(exactDescription, createdFilm.get("description"));
    }

    @Test
    void createFilm_ShouldThrowException_WhenReleaseDateIsNull() {
        Map<String, Object> filmData = new HashMap<>();
        filmData.put("name", validFilm.getName());
        filmData.put("description", validFilm.getDescription());
        filmData.put("releaseDate", null);
        filmData.put("duration", validFilm.getDuration());
        filmData.put("mpa", Map.of("id", 3));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmData));
        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenReleaseDateIsTooEarly() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(Map.of(
                        "name", validFilm.getName(),
                        "description", validFilm.getDescription(),
                        "releaseDate", "1895-12-27",
                        "duration", validFilm.getDuration(),
                        "mpa", Map.of("id", 3)
                )));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void createFilm_ShouldAllowReleaseDateExactly1895_12_28() {
        Map<String, Object> createdFilm = filmController.createFilm(Map.of(
                "name", validFilm.getName(),
                "description", validFilm.getDescription(),
                "releaseDate", "1895-12-28",
                "duration", validFilm.getDuration(),
                "mpa", Map.of("id", 3)
        ));

        assertNotNull(createdFilm.get("id"));
        assertEquals("1895-12-28", createdFilm.get("releaseDate"));
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsNull() {
        Map<String, Object> filmData = new HashMap<>();
        filmData.put("name", validFilm.getName());
        filmData.put("description", validFilm.getDescription());
        filmData.put("releaseDate", validFilm.getReleaseDate().toString());
        filmData.put("duration", null);
        filmData.put("mpa", Map.of("id", 3));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmData));
        assertEquals("Продолжительность фильма должна быть указана", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsNegative() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(Map.of(
                        "name", validFilm.getName(),
                        "description", validFilm.getDescription(),
                        "releaseDate", validFilm.getReleaseDate().toString(),
                        "duration", -10,
                        "mpa", Map.of("id", 3)
                )));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsZero() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(Map.of(
                        "name", validFilm.getName(),
                        "description", validFilm.getDescription(),
                        "releaseDate", validFilm.getReleaseDate().toString(),
                        "duration", 0,
                        "mpa", Map.of("id", 3)
                )));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenMpaRatingIsNull() {
        Map<String, Object> createdFilm = filmController.createFilm(Map.of(
                "name", validFilm.getName(),
                "description", validFilm.getDescription(),
                "releaseDate", validFilm.getReleaseDate().toString(),
                "duration", validFilm.getDuration()
        ));

        assertNotNull(createdFilm.get("id"));
        assertNull(createdFilm.get("mpa"));
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsNull() {
        Map<String, Object> createdFilm = filmController.createFilm(Map.of(
                "name", validFilm.getName(),
                "description", validFilm.getDescription(),
                "releaseDate", validFilm.getReleaseDate().toString(),
                "duration", validFilm.getDuration(),
                "mpa", Map.of("id", 3)
        ));

        assertNotNull(createdFilm.get("id"));
        assertEquals(0, ((java.util.List<?>) createdFilm.get("genres")).size());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsEmpty() {
        Map<String, Object> createdFilm = filmController.createFilm(Map.of(
                "name", validFilm.getName(),
                "description", validFilm.getDescription(),
                "releaseDate", validFilm.getReleaseDate().toString(),
                "duration", validFilm.getDuration(),
                "mpa", Map.of("id", 3),
                "genres", new java.util.ArrayList<>()
        ));

        assertNotNull(createdFilm.get("id"));
        assertEquals(0, ((java.util.List<?>) createdFilm.get("genres")).size());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenAllFieldsAreValid() {
        Map<String, Object> filmData = Map.of(
                "name", validFilm.getName(),
                "description", validFilm.getDescription(),
                "releaseDate", validFilm.getReleaseDate().toString(),
                "duration", validFilm.getDuration(),
                "mpa", Map.of("id", 3),
                "genres", java.util.List.of(Map.of("id", 1), Map.of("id", 6))
        );

        Map<String, Object> createdFilm = filmController.createFilm(filmData);

        assertNotNull(createdFilm.get("id"));
        assertEquals(validFilm.getName(), createdFilm.get("name"));
        assertEquals(validFilm.getDescription(), createdFilm.get("description"));
        assertEquals(validFilm.getReleaseDate().toString(), createdFilm.get("releaseDate"));
        assertEquals(validFilm.getDuration(), createdFilm.get("duration"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mpa = (Map<String, Object>) createdFilm.get("mpa");
        assertNotNull(mpa);
        assertEquals(3, mpa.get("id"));
        assertEquals("PG_13", mpa.get("name"));

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> genres = (java.util.List<Map<String, Object>>) createdFilm.get("genres");
        assertEquals(2, genres.size());
    }

    @Test
    void createFilm_ShouldGenerateNewId_ForMultipleFilms() {
        Map<String, Object> firstFilmData = Map.of(
                "name", "Первый фильм",
                "description", "Описание первого",
                "releaseDate", "2020-01-01",
                "duration", 120,
                "mpa", Map.of("id", 3)
        );
        Map<String, Object> firstFilm = filmController.createFilm(firstFilmData);

        Map<String, Object> secondFilmData = Map.of(
                "name", "Второй фильм",
                "description", "Описание второго",
                "releaseDate", "2021-01-01",
                "duration", 90,
                "mpa", Map.of("id", 4)
        );
        Map<String, Object> createdSecond = filmController.createFilm(secondFilmData);

        assertEquals(1L, firstFilm.get("id"));
        assertEquals(2L, createdSecond.get("id"));
    }
}