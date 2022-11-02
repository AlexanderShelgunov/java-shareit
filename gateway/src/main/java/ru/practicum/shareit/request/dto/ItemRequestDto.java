package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemRequestDto {
    @NonNull
    private String description;
    private LocalDateTime created;
}
