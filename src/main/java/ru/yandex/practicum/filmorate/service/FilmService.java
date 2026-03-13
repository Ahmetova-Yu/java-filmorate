package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public Film createFilm(Film film) {
        validateMpaAndGenres(film);
        Film createdFilm = filmStorage.createFilm(film);
        return enrichFilmWithDetails(createdFilm);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.containsFilm(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        validateMpaAndGenres(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        return enrichFilmWithDetails(updatedFilm);
    }

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms().stream()
                .map(this::enrichFilmWithDetails)
                .toList();
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        return enrichFilmWithDetails(film);
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).addLike(filmId, userId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).removeLike(filmId, userId);
        }
    }

    public List<Film> getMostPopularFilms(Integer count) {
        int limit = count != null ? count : 10;
        Collection<Film> films;

        if (filmStorage instanceof FilmDbStorage) {
            films = ((FilmDbStorage) filmStorage).getMostPopularFilms(limit);
        } else {
            films = filmStorage.findAllFilms().stream()
                    .sorted((f1, f2) -> {
                        int likes1 = filmStorage instanceof FilmDbStorage ?
                                ((FilmDbStorage) filmStorage).getLikesCount(f1.getId()) : 0;
                        int likes2 = filmStorage instanceof FilmDbStorage ?
                                ((FilmDbStorage) filmStorage).getLikesCount(f2.getId()) : 0;
                        return Integer.compare(likes2, likes1);
                    })
                    .limit(limit)
                    .toList();
        }

        return films.stream()
                .map(this::enrichFilmWithDetails)
                .toList();
    }

    private Film enrichFilmWithDetails(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> sortedGenres = new ArrayList<>(film.getGenres());
            sortedGenres.sort(Comparator.comparingInt(Genre::getId));
            film.setGenres(new LinkedHashSet<>(sortedGenres));
        }
        return film;
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            if (!mpaStorage.existsById(film.getMpa().getId())) {
                log.error("Неверный id MPA: {}", film.getMpa().getId());
                throw new NotFoundException("Рейтинг mpa с id " + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    log.error("Неверный id жанра: {}", genre.getId());
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
    }
}