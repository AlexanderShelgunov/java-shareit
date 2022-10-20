package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutputDto;

import java.util.List;

@Service
public interface ItemService {

    List<ItemOutputDto> getAllItemsByOwner(Long ownerId);

    ItemOutputDto getItemById(Long itemId, Long userId);

    List<ItemInputDto> searchItems(String query);

    ItemInputDto createItem(ItemInputDto itemDto, Long ownerId);

    ItemInputDto updateItem(Long itemId, ItemInputDto itemDto, Long userId);

    void deleteItem(Long itemId);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
