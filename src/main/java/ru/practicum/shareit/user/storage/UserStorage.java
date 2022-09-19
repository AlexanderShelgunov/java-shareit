package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public interface UserStorage {

    User createUser(User user);

    User getUser(Long userId);

    User updateUser(User user, Long userId);

    void deleteUser(Long userId);

    List<User> getAllUsers();
}
