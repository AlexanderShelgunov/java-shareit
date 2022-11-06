package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class InputBookingDto {

    @NonNull
    private Long itemId;

    @NonNull
    @FutureOrPresent
    private LocalDateTime start;

    @NonNull
    @Future
    private LocalDateTime end;
}

