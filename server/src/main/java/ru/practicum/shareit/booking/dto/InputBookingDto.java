package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class InputBookingDto {
    private Long id;

    @NonNull
    private Long itemId;

    @NonNull
    private LocalDateTime start;

    @NonNull
    private LocalDateTime end;
}

