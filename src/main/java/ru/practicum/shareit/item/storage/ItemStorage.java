package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Component
public interface ItemStorage {

    Item addNewItem(Item item);

    Item editItem(Item item, Long itemId);

    Item getItem(Long itemId);

    List<Item> getAllItemsByOwner();

    List<Item> searchItem();
}
