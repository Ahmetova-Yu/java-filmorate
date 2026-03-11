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
                () -> filmController.createFilm(validFilm));
        assertEquals("Имя фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenNameIsNull() {
        validFilm.setName(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Имя фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDescriptionIsTooLong() {
        validFilm.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenDescriptionIsExactly200Chars() {
        validFilm.setDescription("a".repeat(200));

        Map<String, Object> createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.get("id"));
        assertEquals(200, validFilm.getDescription().length());
    }

    @Test
    void createFilm_ShouldThrowException_WhenReleaseDateIsNull() {
        validFilm.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenReleaseDateIsTooEarly() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void createFilm_ShouldAllowReleaseDateExactly1895_12_28() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));

        Map<String, Object> createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.get("id"));
        assertEquals(LocalDate.of(1895, 12, 28), validFilm.getReleaseDate());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsNull() {
        validFilm.setDuration(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть указана", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsNegative() {
        validFilm.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsZero() {
        validFilm.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenMpaRatingIsNull() {
        validFilm.setMpaRating(null);

        Map<String, Object> createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.get("id"));
        assertNull(createdFilm.get("mpa"));
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsNull() {
        validFilm.setGenres(null);

        Map<String, Object> createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.get("id"));
        assertEquals(0, ((java.util.List<?>) createdFilm.get("genres")).size());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsEmpty() {
        validFilm.setGenres(Set.of());

        Map<String, Object> createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.get("id"));
        assertEquals(0, ((java.util.List<?>) createdFilm.get("genres")).size());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenAllFieldsAreValid() {
        Map<String, Object> createdFilm = filmController.createFilm(validFilm);

        assertNotNull(createdFilm.get("id"));
        assertEquals(1L, createdFilm.get("id"));
        assertEquals(validFilm.getName(), createdFilm.get("name"));
        assertEquals(validFilm.getDescription(), createdFilm.get("description"));
        assertEquals(validFilm.getReleaseDate().toString(), createdFilm.get("releaseDate").toString());
        assertEquals(validFilm.getDuration(), createdFilm.get("duration"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mpa = (Map<String, Object>) createdFilm.get("mpa");
        assertNotNull(mpa);
        assertEquals(3, mpa.get("id")); // PG_13 ordinal + 1 = 3
        assertEquals("PG_13", mpa.get("name"));

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Integer>> genres = (java.util.List<Map<String, Integer>>) createdFilm.get("genres");
        assertEquals(2, genres.size());
    }

    @Test
    void createFilm_ShouldGenerateNewId_ForMultipleFilms() {
        Map<String, Object> firstFilm = filmController.createFilm(validFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Второй фильм");
        secondFilm.setDescription("Описание 2");
        secondFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        secondFilm.setDuration(90);
        secondFilm.setMpaRating(MpaRating.R);

        Map<String, Object> createdSecond = filmController.createFilm(secondFilm);

        assertEquals(1L, firstFilm.get("id"));
        assertEquals(2L, createdSecond.get("id"));
    }
}