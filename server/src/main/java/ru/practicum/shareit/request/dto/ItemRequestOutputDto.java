package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemInputDto;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemRequestOutputDto {
    private Long id;
    @NonNull
    private String description;
    private LocalDateTime created;
    private List<ItemInputDto> items;
}
