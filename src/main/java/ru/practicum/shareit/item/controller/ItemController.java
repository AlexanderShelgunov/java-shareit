package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.CreateItemValidate;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    ItemService itemService;

    @PostMapping
    public ItemDto addNewItem(@Validated({CreateItemValidate.class}) @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId) {

        if (itemDto.getAvailable() == null) {
            log.info("У предмета {} должен быть статус доступности ", itemDto);
            throw new ValidateException("У предмета " + itemDto +
                    " должен быть статус доступности");
        }

        if (!itemDto.getAvailable()) {
            log.info("Предмет {} не доступна ", itemDto);
            throw new ValidateException("Предмет " + itemDto +
                    " не доступна");
        }

        checkUserOwner(userId);
        log.info("добавляемый предмет: {}", itemDto);
        return itemService.addNewItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto editItem(@RequestBody ItemDto itemDto,
                            @PathVariable Long itemId,
                            @RequestHeader("X-Sharer-User-Id") Long userId) {

        ItemDto itemDtoReturned = itemService.editItem(itemDto, itemId, userId);

        if (itemDtoReturned == null) {
            throw new NotFoundException("Предмет с ID = " + itemId +
                    " не принадлежит пользователю с ID = " + userId);
        }

        checkUserOwner(userId);
        log.info("Новые данные для редактирования: {}", itemDto);

        return itemDtoReturned;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        log.info("Поиск предмета по ID= {}", itemId);
        return itemService.getItem(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text,
                                    @RequestHeader("X-Sharer-User-Id") Long UserId) {

        if (text.equals("")) {
            return new ArrayList<>();
        }

        log.info("Поиск предмета по тексту = {}", text);
        return itemService.searchItem(UserId, text);
    }


    public void checkUserOwner(Long UserId) {
        UserDto user = itemService.getUserOwner(UserId);

        if (user == null) {
            log.info("Владелец вещи с ID = {} не найден ", UserId);
            throw new NotFoundException("Владелец вещи с ID = " + UserId +
                    " не найден");
        }
    }

}
