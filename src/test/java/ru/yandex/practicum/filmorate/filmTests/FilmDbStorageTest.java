package ru.yandex.practicum.filmorate.filmTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

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

    private Mpa createMpa(int id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }

    private Genre createGenre(int id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(createMpa(3, "PG-13"));

        Set<Genre> genres = new HashSet<>();
        genres.add(createGenre(1, "Комедия"));
        genres.add(createGenre(6, "Боевик"));
        film.setGenres(genres);

        Film created = filmStorage.createFilm(film);

        assertThat(created.getId()).isNotNull();

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Film");
        assertThat(found.get().getMpa().getId()).isEqualTo(3);
        assertThat(found.get().getMpa().getName()).isEqualTo("PG-13");
        assertThat(found.get().getGenres()).hasSize(2);
    }

    @Test
    void testCreateFilmWithoutGenres() {
        Film film = new Film();
        film.setName("Test Film No Genres");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(createMpa(3, "PG-13"));
        film.setGenres(null);

        Film created = filmStorage.createFilm(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getGenres()).isNullOrEmpty();
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
        film.setMpa(createMpa(3, "PG-13"));
        Film createdFilm = filmStorage.createFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Set<Long> likes = filmStorage.getLikesForFilm(createdFilm.getId());
        assertThat(likes).contains(createdUser.getId());
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
        film.setMpa(createMpa(3, "PG-13"));
        Film createdFilm = filmStorage.createFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());

        Set<Long> likes = filmStorage.getLikesForFilm(createdFilm.getId());
        assertThat(likes).doesNotContain(createdUser.getId());
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(createMpa(3, "PG-13"));

        Film created = filmStorage.createFilm(film);

        created.setName("Updated Film");
        created.setMpa(createMpa(4, "R"));

        Set<Genre> genres = new HashSet<>();
        genres.add(createGenre(2, "Драма"));
        created.setGenres(genres);

        Film updated = filmStorage.updateFilm(created);

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Film");
        assertThat(found.get().getMpa().getId()).isEqualTo(4);
        assertThat(found.get().getMpa().getName()).isEqualTo("R");
        assertThat(found.get().getGenres()).hasSize(1);
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(createMpa(3, "PG-13"));

        Film created = filmStorage.createFilm(film);

        boolean deleted = filmStorage.deleteFilm(created.getId());
        assertThat(deleted).isTrue();

        Optional<Film> found = filmStorage.getFilmById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testGetMostPopularFilms() {
        // Создаем пользователей
        User user1 = createUser("user1@test.com", "user1");
        User user2 = createUser("user2@test.com", "user2");
        User user3 = createUser("user3@test.com", "user3");

        // Создаем фильмы
        Film film1 = createFilm("Film 1", 100);
        Film film2 = createFilm("Film 2", 110);
        Film film3 = createFilm("Film 3", 120);

        // Добавляем лайки: film1 - 3 лайка, film2 - 2 лайка, film3 - 1 лайк
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user3.getId());

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        filmStorage.addLike(film3.getId(), user1.getId());

        // Получаем топ-2 популярных фильмов
        Collection<Film> popularFilms = filmStorage.getMostPopularFilms(2);

        assertThat(popularFilms).hasSize(2);

        // Проверяем порядок: сначала film1 (3 лайка), потом film2 (2 лайка)
        Film[] films = popularFilms.toArray(new Film[0]);
        assertThat(films[0].getName()).isEqualTo("Film 1");
        assertThat(films[1].getName()).isEqualTo("Film 2");
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
    }

    private Film createFilm(String name, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(duration);
        film.setMpa(createMpa(3, "PG-13"));
        return filmStorage.createFilm(film);
    }

    @Test
    void testContainsFilm() {
        Film film = createFilm("Test Film", 120);

        assertThat(filmStorage.containsFilm(film.getId())).isTrue();
        assertThat(filmStorage.containsFilm(999L)).isFalse();
    }

    @Test
    void testGetLikesCount() {
        User user1 = createUser("user1@test.com", "user1");
        User user2 = createUser("user2@test.com", "user2");

        Film film = createFilm("Test Film", 120);

        filmStorage.addLike(film.getId(), user1.getId());
        filmStorage.addLike(film.getId(), user2.getId());

        int likesCount = filmStorage.getLikesCount(film.getId());
        assertThat(likesCount).isEqualTo(2);
    }
}