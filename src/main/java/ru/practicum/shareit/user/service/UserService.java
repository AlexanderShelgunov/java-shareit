package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUser(Long id);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, Long userId);

    void deleteUser(Long id);
}
