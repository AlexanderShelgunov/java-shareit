package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private String request;

}


