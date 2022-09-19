package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public interface ItemService {

    ItemDto addNewItem(ItemDto itemDto, Long userId);

    ItemDto editItem(ItemDto itemDto, Long itemId, Long userId);

    ItemDto getItem(Long itemId);

    UserDto getUserOwner(Long userId);

    List<ItemDto> getAllItemsByOwner(Long userId);

    List<ItemDto> searchItem(Long userId, String text);
}
