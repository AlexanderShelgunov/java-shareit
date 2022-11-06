package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "email")
public class UpdateUserDto {
    private Long id;
    private String name;
    @Email
    private String email;
}
