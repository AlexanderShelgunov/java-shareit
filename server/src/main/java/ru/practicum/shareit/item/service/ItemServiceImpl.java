package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemOutputDto> getAllItemsByOwner(Long ownerId, Integer from, Integer size) {
        return itemRepository.findAll(getPageRequest(from, size)).stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .map(item -> convertToItemOutputDto(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemOutputDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("?????????????? c id=" + itemId + " ???? ????????????."));
        return convertToItemOutputDto(item, userId);
    }

    @Override

    public List<ItemInputDto> searchItems(String query, Integer from, Integer size) {
        if (query.isEmpty() || query.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemRepository.search(query, getPageRequest(from, size)).stream()
                    .filter(Item::getAvailable)
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public ItemInputDto createItem(ItemInputDto itemDto, Long ownerId) {
        validate(itemDto);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("???????????????????????? c id=" + ownerId + " ???? ????????????."));
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("???????????? c id=" + itemDto.getRequestId() + " ???? ????????????."));
        }
        Item item = ItemMapper.fromItemDto(itemDto, owner, itemRequest);
        ItemInputDto itemInputDto = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("???????????? ?????????????? ?? id={}", itemInputDto.getId());
        return itemInputDto;
    }

    @Override
    @Transactional
    public ItemInputDto updateItem(Long itemId, ItemInputDto itemDto, Long userId) {
        Item itemUpd = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("?????????????? c id=" + itemId + " ???? ????????????."));
        if (!Objects.equals(userId, itemUpd.getOwner().getId())) {
            log.warn("?????????????????????????? ???????? ?????????? ???????????? ???? ????????????????.");
            throw new ForbiddenException("?????????????????????????? ???????? ?????????? ???????????? ???? ????????????????.");
        }
        if (itemDto.getName() != null) {
            itemUpd.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemUpd.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemUpd.setAvailable(itemDto.getAvailable());
        }
        ItemInputDto itemUpdInputDto = ItemMapper.toItemDto(itemRepository.save(itemUpd));
        log.info("?????????????? ?????????????? ?? id={}", itemUpdInputDto.getId());
        return itemUpdInputDto;
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
        log.info("???????????? ?????????????? ?? id={}", itemId);
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        Booking booking = bookingRepository.findAllByBookerId(userId).stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new ValidateException("???????????????????????? c id=" + userId +
                        " ???? ?????????? ????????????????????????, ?? ?????????????? ?????????? ???????????????? ??????????????????????."));
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("???????????????????????? c id=" + userId + " ???? ????????????."));
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NotFoundException("?????????????? c id=" + itemId + " ???? ????????????."));
            Comment comment = CommentMapper.fromCommentDto(commentDto, author, item);
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new ValidateException("???????????? ???????????????????????? - " + booking.getStatus().toString() +
                    ". ???????????????????? ?????????????????????? ???? ??????????????????.");
        }
    }

    private void validate(ItemInputDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new ValidateException("?????????????????????? ?????????????? ????????????????.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            throw new ValidateException("?????????????????????? ?????????????????????? ????????????????.");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidateException("?????????????????????? ????????????.");
        }
    }

    private ItemOutputDto convertToItemOutputDto(Item item, Long ownerId) {
        Booking lastBooking = bookingRepository.findLastBooking(item.getId(), ownerId).orElse(null);
        Booking nextBooking = bookingRepository.findNextBooking(item.getId(), ownerId).orElse(null);
        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        ItemOutputDto itemOutputDto = ItemMapper.toItemOutputDto(item);
        if (lastBooking != null) {
            itemOutputDto.setLastBooking(BookingMapper.toBookingDtoForItem(lastBooking));
        }
        if (nextBooking != null) {
            itemOutputDto.setNextBooking(BookingMapper.toBookingDtoForItem(nextBooking));
        }
        itemOutputDto.setComments(comments);
        return itemOutputDto;
    }

    private PageRequest getPageRequest(Integer from, Integer size) {
        int page = from < size ? 0 : from / size;

        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }
}
