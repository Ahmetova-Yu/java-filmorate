package ru.yandex.practicum.filmorate.filmTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);
        film.setGenres(Set.of(Genre.COMEDY, Genre.ACTION));

        Film created = filmStorage.createFilm(film);

        assertThat(created.getId()).isNotNull();

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Film");
        assertThat(found.get().getMpaRating()).isEqualTo(MpaRating.PG_13);
        assertThat(found.get().getGenres()).hasSize(2);
    }

    @Test
    void testAddLike() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.createUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);
        Film createdFilm = filmStorage.createFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Film liked = filmStorage.getFilmById(createdFilm.getId()).get();
        assertThat(liked.getLikes()).contains(createdUser.getId());
    }

    @Test
    void testRemoveLike() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.createUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);
        Film createdFilm = filmStorage.createFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());

        Film liked = filmStorage.getFilmById(createdFilm.getId()).get();
        assertThat(liked.getLikes()).doesNotContain(createdUser.getId());
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);

        Film created = filmStorage.createFilm(film);

        created.setName("Updated Film");
        created.setMpaRating(MpaRating.R);
        created.setGenres(Set.of(Genre.DRAMA));

        filmStorage.updateFilm(created);

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Film");
        assertThat(found.get().getMpaRating()).isEqualTo(MpaRating.R);
        assertThat(found.get().getGenres()).hasSize(1);
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);

        Film created = filmStorage.createFilm(film);

        boolean deleted = filmStorage.deleteFilm(created.getId());
        assertThat(deleted).isTrue();

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testGetFilmByIdNotFound() {
        Optional<Film> found = filmStorage.getFilmById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testContainsFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);

        Film created = filmStorage.createFilm(film);

        boolean contains = filmStorage.containsFilm(created.getId());
        assertThat(contains).isTrue();

        boolean notContains = filmStorage.containsFilm(999L);
        assertThat(notContains).isFalse();
    }

    @Test
    void testGetFilmsCount() {
        int initialCount = filmStorage.getFilmsCount();

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);

        filmStorage.createFilm(film);

        int newCount = filmStorage.getFilmsCount();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}