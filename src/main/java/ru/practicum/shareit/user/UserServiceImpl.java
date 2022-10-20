package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl {

    @Autowired
    private UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(newUser);
        return UserMapper.toUserDto(savedUser);
    }

    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID = " + userId + " не найден "));

        return UserMapper.toUserDto(user);
    }

    @Transactional
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = UserMapper.toUser(userDto);
        User userWrap = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден " + userDto.getId()));

        if (user.getName() != null) {
            userWrap.setName(user.getName());
        }

        if (user.getEmail() != null) {
            userWrap.setEmail(user.getEmail());
        }

        userRepository.save(userWrap);
        return UserMapper.toUserDto(userWrap);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
