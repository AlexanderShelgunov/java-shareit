package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void shouldFindNoUsersIfRepositoryIsEmpty() {
        Iterable<User> users = userRepository.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldStoreUser() {
        User user1 = new User("User1", "user1@mail.ru");
        entityManager.persist(user1);

        assertThat(user1).hasFieldOrPropertyWithValue("name", "User1");
        assertThat(user1).hasFieldOrPropertyWithValue("email", "user1@mail.ru");

    }

    @Test
    public void shouldFindAllUsers() {
        User user1 = new User("User1", "user1@mail.ru");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@mail.ru");
        entityManager.persist(user2);

        User user3 = new User("User3", "user3@mail.ru");
        entityManager.persist(user3);

        Iterable<User> users = userRepository.findAll();

        assertThat(users).hasSize(3).contains(user1, user2, user3);

    }

    @Test
    public void shouldFindUserById() {
        User user1 = new User("User1", "user1@mail.ru");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@mail.ru");
        entityManager.persist(user2);

        User foundUser = userRepository.findById(user2.getId()).get();

        assertThat(foundUser).isEqualTo(user2);

    }

    @Test
    public void shouldUpdateUserById() {
        User user1 = new User("User1", "user1@mail.ru");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@mail.ru");
        entityManager.persist(user2);

        UserDto updatedUser = UserDto.builder().id(2L).name("updatedUser2").email("user2updated@mail.ru").build();
        userRepository.save(UserMapper.toUser(updatedUser));

        User checkUser = userRepository.findById(user2.getId()).get();

        assertThat(checkUser.getId()).isEqualTo(user2.getId());
        assertThat(checkUser.getName()).isEqualTo("updatedUser2");
        assertThat(checkUser.getEmail()).isEqualTo("user2updated@mail.ru");

    }

    @Test
    public void shouldDeleteUserById() {
        User user1 = new User("User1", "user1@mail.ru");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@mail.ru");
        entityManager.persist(user2);

        User user3 = new User("User3", "user3@mail.ru");
        entityManager.persist(user3);

        userRepository.deleteById(user2.getId());

        Iterable<User> users = userRepository.findAll();
        assertThat(users).hasSize(2).contains(user1, user3);

    }

}