package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ServerException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.user.dto.CreateUserValidate;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Текущее количество пользователей: {}", userService.getAllUsers().size());
        return userService.getAllUsers();
    }

    @PostMapping
    public UserDto createUser(@Validated({CreateUserValidate.class}) @RequestBody UserDto userDto) {

        if (userDto.getEmail() == null || "".equals(userDto.getEmail()) || !userDto.getEmail().contains("@")) {
            log.info("электронная почта {} не корректная", userDto.getEmail());
            throw new ValidateException("электронная почта " + userDto.getEmail() +
                    " не может быть пустой и должна содержать символ @");
        }
        checkEmailIsFree(userDto);
        log.info("Сохраняемый пользователь: {}", userDto);
        return userService.createUser(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Пользователь {} полученый по ID={}", userService.getUser(userId), userId);
        return userService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        UserDto findUser = userService.getUser(userId);

        if (findUser == null) {
            log.info("Пользователь с ID = {} не найден", userId);
            throw new NotFoundException("Пользователь с ID = " + userId +
                    " не найден");
        }

        checkEmailIsFree(userDto);
        log.info("Обновляемый пользователь: {}", userDto);
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаляемый пользователь {}", userService.getUser(userId));
        userService.deleteUser(userId);
    }

    public void checkEmailIsFree(UserDto userDto) {
        List<UserDto> users = userService.getAllUsers();

        if (users.contains(userDto)) {
            log.info("Пользователь с почтой {} уже существует ", userDto.getEmail());
            throw new ServerException("Пользователь с почтой " + userDto.getEmail() +
                    " уже существует");
        }
    }
}
