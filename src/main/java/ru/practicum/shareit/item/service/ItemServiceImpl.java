package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    ItemStorage itemStorage;
    @Autowired
    UserService userService;

    @Override
    public ItemDto addNewItem(ItemDto itemDto, Long userId) {
        User owner = UserMapper.toUser(userService.getUser(userId));
        Item newItem = ItemMapper.toItem(itemDto);
        newItem.setOwner(owner);

        return ItemMapper.toItemDto(itemStorage.addNewItem(newItem));
    }

    @Override
    public ItemDto editItem(ItemDto itemDto, Long itemId, Long userId) {
        Item updatedItem = itemStorage.getItem(itemId);
        long ownerId = updatedItem.getOwner().getId();

        if (ownerId != userId) {
            return null;
        }

        Item incomeItem = ItemMapper.toItem(itemDto);
        updatedItem = itemStorage.editItem(incomeItem, itemId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItem(Long itemId) {

        Item item = itemStorage.getItem(itemId);

        if (item == null) {
            return null;
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long userId) {
        return itemStorage.getAllItemsByOwner()
                .stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(Long userId, String text) {
        return itemStorage.searchItem()
                .stream()
                .filter(item -> (
                        (item.getAvailable() && item.getName().toLowerCase().contains(text.toLowerCase()))) ||
                        (item.getAvailable() && item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserOwner(Long userId) {
        return userService.getUser(userId);
    }
}
