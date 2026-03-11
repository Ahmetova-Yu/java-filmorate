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

        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertEquals(200, createdFilm.getDescription().length());
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

        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
    }

    @Test
    void createFilm_ShouldThrowException_WhenDurationIsNull() {
        validFilm.setDuration(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
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

        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertNull(createdFilm.getMpaRating());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsNull() {
        validFilm.setGenres(null);

        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertNull(createdFilm.getGenres());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenGenresIsEmpty() {
        validFilm.setGenres(Set.of());

        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertTrue(createdFilm.getGenres().isEmpty());
    }

    @Test
    void createFilm_ShouldCreateFilm_WhenAllFieldsAreValid() {
        Film createdFilm = filmController.createFilm(validFilm);

        assertNotNull(createdFilm.getId());
        assertEquals(1, createdFilm.getId());
        assertEquals(validFilm.getName(), createdFilm.getName());
        assertEquals(validFilm.getDescription(), createdFilm.getDescription());
        assertEquals(validFilm.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(validFilm.getDuration(), createdFilm.getDuration());
        assertEquals(validFilm.getMpaRating(), createdFilm.getMpaRating());
        assertEquals(validFilm.getGenres(), createdFilm.getGenres());
    }

    @Test
    void createFilm_ShouldGenerateNewId_ForMultipleFilms() {
        Film firstFilm = filmController.createFilm(validFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Второй фильм");
        secondFilm.setDescription("Описание 2");
        secondFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        secondFilm.setDuration(90);
        secondFilm.setMpaRating(MpaRating.R);

        Film createdSecond = filmController.createFilm(secondFilm);

        assertEquals(1L, firstFilm.getId());
        assertEquals(2L, createdSecond.getId());
    }
}