package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private MpaRating mpaRating;

    public Mpa getMpa() {
        if (mpaRating == null) return null;
        Mpa mpa = new Mpa();
        mpa.setId(mpaRating.ordinal() + 1);
        mpa.setName(mpaRating.name());
        return mpa;
    }
}