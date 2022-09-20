package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemDto {
    private Long id;
    @NotBlank(groups = {CreateItemValidate.class})
    private String name;
    @NotBlank(groups = {CreateItemValidate.class})
    private String description;

    private Boolean available;
    private User owner;
    private String request;

    public ItemDto(Long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
