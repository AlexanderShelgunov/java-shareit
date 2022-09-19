package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "email")
public class UserDto {
    private Long id;
    @NotBlank(groups = {CreateUserValidate.class})
    private String name;

    @Email
    @NotBlank(groups = {CreateUserValidate.class})
    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
