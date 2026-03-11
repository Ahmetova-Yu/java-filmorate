package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).addLike(filmId, userId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: фильм {}, пользователь {}", filmId, userId);

        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).removeLike(filmId, userId);
        }
    }

    public Collection<Film> getMostPopularFilms(Integer count) {
        int limit = count != null ? count : 10;
        log.info("Запрос на получение {} самых популярных фильмов", limit);

        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getMostPopularFilms(limit);
        }

        return filmStorage.findAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(limit)
                .toList();
    }
}