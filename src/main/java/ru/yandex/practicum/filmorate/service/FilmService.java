package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private FilmStorage filmStorage;
    private UserStorage userStorage;

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.containsFilm(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }

        return filmStorage.updateFilm(film);
    }

    public Collection<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", id);
                    return new ValidationException("Фильм с id " + id + " не найден");
                });
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Запрос на добавление лайка: пользователь {} ставит лайк фильму {}", userId, filmId);

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new ValidationException("Фильм с id " + filmId + " не найден");
                });

        userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ValidationException("Пользователь с id " + userId + " не найден");
                });

        film.getLikes().add(userId);

        log.info("Лайк добавлен. У фильма {} теперь {} лайков", filmId, film.getLikes().size());
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: пользователь {} убирает лайк с фильма {}", userId, filmId);

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new ValidationException("Фильм с id " + filmId + " не найден");
                });

        userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ValidationException("Пользователь с id " + userId + " не найден");
                });

        if (!film.getLikes().contains(userId)) {
            log.error("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь с id " + userId + " не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);

        log.info("Лайк удален. У фильма {} теперь {} лайков", filmId, film.getLikes().size());
    }

    public Collection<Film> getMostPopularFilms(Integer count) {
        int limit = count != null ? count : 10;
        log.info("Запрос на получение {} самых популярных фильмов", limit);

        return filmStorage.findAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}