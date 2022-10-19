package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class InputBookingDto {
    private Long id;

    @NonNull
    private Long itemId;

    @NonNull
    @Future
    private LocalDateTime start;

    @NonNull
    @Future
    private LocalDateTime end;
}
