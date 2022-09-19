package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        User savedUser = userStorage.createUser(newUser);
        return UserMapper.toUserDto(savedUser);
    }

    public UserDto getUser(Long userId) {
        User user = userStorage.getUser(userId);

        if (user == null) {
            return null;
        }

        return UserMapper.toUserDto(user);
    }

    public UserDto updateUser(UserDto userDto, Long userId) {
        User newUser = UserMapper.toUser(userDto);
        User updateUser = userStorage.updateUser(newUser, userId);
        return UserMapper.toUserDto(updateUser);
    }

    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
    }
}
