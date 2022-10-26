package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public List<ItemOutputDto> getAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                  @RequestParam(required = false, defaultValue = "0")
                                                  @PositiveOrZero Integer from,
                                                  @RequestParam(required = false, defaultValue = "20")
                                                      @PositiveOrZero Integer size) {
        return itemService.getAllItemsByOwner(ownerId, from, size);
    }

    @GetMapping("{itemId}")
    public ItemOutputDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("search")
    public List<ItemInputDto> searchItems(@RequestParam String text,
                                          @RequestParam(required = false, defaultValue = "0")
                                          @PositiveOrZero Integer from,
                                          @RequestParam(required = false, defaultValue = "20")
                                              @PositiveOrZero Integer size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping
    public ItemInputDto createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                   @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemInputDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long itemId,
                                   @Valid @NotNull @RequestBody ItemInputDto itemDto) {
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId,
                                    @Valid @NotNull @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }

}
