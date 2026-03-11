package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    public Film createFilm(Film film) {
        log.info("Запрос на создание фильма");
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Запрос на обновление фильма: {}", film.getId());

        if (!filmStorage.containsFilm(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        return filmStorage.updateFilm(film);
    }

    public Collection<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Запрос на добавление лайка: фильм {}, пользователь {}", filmId, userId);

        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: фильм {}, пользователь {}", filmId, userId);

        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getMostPopularFilms(Integer count) {
        int limit = count != null ? count : 10;
        log.info("Запрос на получение {} самых популярных фильмов", limit);

        return filmStorage.getMostPopularFilms(limit);
    }
}