package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequestOutputDto findRequestById(Long requestId, Long userId);

    List<ItemRequestOutputDto> findAllRequestFromRequester(Long requesterId);

    List<ItemRequestOutputDto> findAllRequest(Long userId, Integer from, Integer size);
}
