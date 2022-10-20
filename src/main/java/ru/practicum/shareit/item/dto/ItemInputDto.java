package ru.practicum.shareit.item.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemInputDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
}