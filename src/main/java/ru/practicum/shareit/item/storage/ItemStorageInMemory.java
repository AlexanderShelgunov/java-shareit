package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemStorageInMemory implements ItemStorage {

    private Long idGenerator = 0L;
    private Map<Long, Item> items = new HashMap();

    @Override
    public Item addNewItem(Item item) {
        item.setId(++idGenerator);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item editItem(Item item, Long itemId) {
        Item updatedItem = items.get(itemId);

        if (updatedItem == null) {
            return null;
        }

        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }


        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        items.put(itemId, updatedItem);
        return updatedItem;
    }

    @Override
    public Item getItem(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllItemsByOwner() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> searchItem() {
        return new ArrayList<>(items.values());
    }
}
