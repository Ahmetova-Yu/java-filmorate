package ru.yandex.practicum.filmorate.filmTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

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
    void testAddLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG_13);

        Film created = filmStorage.createFilm(film);

        filmStorage.addLike(created.getId(), 1L);

        Film liked = filmStorage.getFilmById(created.getId()).get();
        assertThat(liked.getLikes()).contains(1L);
    }
}