package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                        @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutputDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable @NotNull Long requestId) {
        return itemRequestService.findRequestById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestOutputDto> getAllRequestsFromRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        return itemRequestService.findAllRequestFromRequester(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestOutputDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                     @RequestParam(required = false, defaultValue = "0")
                                                     @PositiveOrZero Integer from,
                                                     @RequestParam(required = false, defaultValue = "20")
                                                     @PositiveOrZero Integer size) {
        return itemRequestService.findAllRequest(requesterId, from, size);
    }
}
