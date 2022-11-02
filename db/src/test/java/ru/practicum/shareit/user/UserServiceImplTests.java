package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;

    private final User user = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("UserName2")
            .email("user2@mail.ru")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru")
            .build();

    @Test
    void testMapperFromUserToUserDto() {
        UserDto result = UserMapper.toUserDto(user);
        assertEquals(result.getId(), user.getId());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
    }

    @Test
    void testMapperFromUserDtoToUser() {
        User result = UserMapper.toUser(userDto);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.createUser(userDto);
        assertEquals(result, userDto);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testCreateUserWithNoEmailShouldThrowException() {
        ValidateException exception = assertThrows(ValidateException.class,
                () -> userService.createUser(UserDto.builder()
                        .name("UserNameUpdate").build()));
        assertEquals("Не указана почта пользователя.", exception.getMessage());
    }

    @Test
    void testUpdateUserName() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserNameUpdate")
                .email("user@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.updateUser(UserDto.builder().name("UserNameUpdate").build(), 1L);
        assertEquals(result.getName(), "UserNameUpdate");
        assertEquals(result.getEmail(), "user@mail.ru");

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testUpdateUserEmail() {
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(1L)
                .name("UserName")
                .email("userUpdate@mail.ru")
                .build());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.updateUser(UserDto.builder().email("userUpdate@mail.ru").build(), 1L);
        assertEquals(result.getName(), "UserName");
        assertEquals(result.getEmail(), "userUpdate@mail.ru");

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getUser(anyLong());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetUserByIdWithUnknownIdShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUser(11L));
        assertEquals("Пользователь с ID = 11 не найден", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserDto> result = userService.getAllUsers();
        assertEquals(user.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(result.size(), 2);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(anyLong());
    }
}