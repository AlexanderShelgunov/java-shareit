package ru.practicum.shareit.user.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "email")
public class UserDto {
    private Long id;
    private String name;
    private String email;

}
