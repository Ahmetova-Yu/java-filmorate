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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
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

    public Map<String, Object> getFilmResponse(Film film) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", film.getId());
        response.put("name", film.getName());
        response.put("description", film.getDescription());
        response.put("releaseDate", film.getReleaseDate());
        response.put("duration", film.getDuration());
        response.put("likes", film.getLikes() != null ? film.getLikes() : new HashSet<>());

        List<Map<String, Integer>> genres = new ArrayList<>();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Map<String, Integer> genreMap = new HashMap<>();
                genreMap.put("id", genre.ordinal() + 1);
                genres.add(genreMap);
            }
        }
        response.put("genres", genres);

        if (film.getMpaRating() != null) {
            Map<String, Object> mpa = new HashMap<>();
            mpa.put("id", film.getMpaRating().ordinal() + 1);
            mpa.put("name", film.getMpaRating().name());
            response.put("mpa", mpa);
        } else {
            response.put("mpa", null);
        }

        return response;
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

    public Collection<Film> getMostPopularFilms(Integer count) {
        int limit = count != null ? count : 10;

        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getMostPopularFilms(limit);
        }
        return new ArrayList<>();
    }
}