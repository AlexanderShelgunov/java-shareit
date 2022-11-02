package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CommentDto {
    @Size(min = 1)
    private String text;
    private String authorName;
    private LocalDateTime created;
}
