package ru.practicum.shareit.item.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemInputDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}