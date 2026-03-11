package ru.yandex.practicum.filmorate.model;

public enum GenreEnum {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    CARTOON("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");

    private final String name;

    GenreEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public static GenreEnum fromId(int id) {
        return values()[id - 1];
    }
}