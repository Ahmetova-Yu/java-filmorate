package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDbStorage userStorage;

    public User createUser(User user) {
        log.info("Запрос на создание пользователя");
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        log.info("Запрос на обновление пользователя: {}", user.getId());

        if (!userStorage.containsUser(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        return userStorage.updateUser(user);
    }

    public Collection<User> findAllUsers() {
        return userStorage.findAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление в друзья: {} -> {}", userId, friendId);

        getUserById(userId);
        getUserById(friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей: {} -> {}", userId, friendId);

        getUserById(userId);
        getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getUserFriends(Long userId) {
        log.info("Запрос на получение друзей пользователя {}", userId);

        getUserById(userId);

        return userStorage.getFriendIds(userId).stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос на получение общих друзей пользователей {} и {}", userId, otherId);

        getUserById(userId);
        getUserById(otherId);

        var userFriends = userStorage.getFriendIds(userId);
        var otherFriends = userStorage.getFriendIds(otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}