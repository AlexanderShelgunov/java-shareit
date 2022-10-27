package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id=" + userId + " не найден."));

        if (itemRequestDto.getDescription() == null) {
            throw new ValidateException("Описание не может быть пустым " + itemRequestDto.getDescription());
        }

        ItemRequest itemRequest = ItemRequestMapper.fromItemRequestDto(itemRequestDto, requester);
        ItemRequestDto itemRequestCreated = ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
        log.info("Создан запрос с id={}", itemRequestCreated.getId());
        return itemRequestCreated;
    }

    @Override
    public ItemRequestOutputDto findRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id=" + userId + " не найден."));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос c id=" + requestId + " не найден."));
        List<ItemInputDto> items = getItems(requestId);
        return ItemRequestMapper.itemRequestOutputDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestOutputDto> findAllRequestFromRequester(Long requesterId) {
        userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id=" + requesterId + " не найден."));
        return itemRequestRepository.findItemRequestsByRequestorId(requesterId).stream()
                .map(request -> ItemRequestMapper.itemRequestOutputDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestOutputDto> findAllRequest(Long userId, Integer from, Integer size) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id=" + userId + " не найден."));
        return itemRequestRepository.findAll(getPageRequest(from, size)).stream()
                .filter(itemRequest -> !Objects.equals(itemRequest.getRequestor().getId(), requester.getId()))
                .map(request -> ItemRequestMapper.itemRequestOutputDto(request, getItems(request.getId())))
                .collect(Collectors.toList());
    }

    private List<ItemInputDto> getItems(Long requestId) {
        return itemRepository.findItemByItemRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private PageRequest getPageRequest(Integer from, Integer size) {
        int page = from < size ? 0 : from / size;
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }

}
