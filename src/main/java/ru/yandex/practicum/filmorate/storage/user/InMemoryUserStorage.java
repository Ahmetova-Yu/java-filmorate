package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище: id={}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existingUser = users.get(user.getId());

        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());

        log.debug("Пользователь обновлен в хранилище: id={}", user.getId());
        return existingUser;
    }

    @Override
    public Collection<User> findAllUsers() {
        log.debug("Получены все фильмы из хранилища. Всего пользователей: {}", users.size());
        return users.values();
    }

    private long getNextId() {
        return ++currentId;
    }
}