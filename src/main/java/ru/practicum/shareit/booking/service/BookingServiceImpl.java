package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;


    @Override
    @Transactional
    public BookingDto addBooking(Long bookerId, InputBookingDto inputBookingDto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id=" + bookerId + " не найден."));
        Item item = itemRepository.findById(inputBookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет c id=" + inputBookingDto.getItemId() + " не найден."));

        if (booker.getId().equals(item.getOwner().getId())) {
            log.warn("Пользователь c id={} является владельцем предмета.", bookerId);
            throw new NotFoundException("Пользователь c id=" + bookerId + " является владельцем предмета.");
        }

        Booking booking = BookingMapper.fromInputBookingDto(inputBookingDto, item, booker);

        if (booking.getItem().getAvailable() && booking.getStart().isBefore(booking.getEnd())) {
            BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
            log.info("Добавлено бронирование с id={}", bookingDto.getId());
            return bookingDto;
        } else {
            log.warn("Предмет не доступен для бронирования или указаны неверные параметры бронирования.");
            throw new ValidateException("Неверные параметры бронирования.");
        }
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean isApprove) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование c id=" + bookingId + "не найдено."));
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new NotFoundException("Пользователь c id=" + ownerId + " не является хозяином предмета.");
        }
        if (booking.getStatus().equals(BookingStatus.WAITING)) {
            booking.setStatus(isApprove ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        } else {
            log.warn("Статус бронирования id={} отличается от WAITING", booking.getId());
            throw new ValidateException("Бронирование не нуждается в изменении статуса.");
        }
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(booking));
        log.info("Изменено бронирование с id={}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование c id=" + bookingId + "не найдено."));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            log.warn("Пользователь c id=" + userId + " не имеет отношение к предмету.");
            throw new NotFoundException("Пользователь c id=" + userId + " не имеет отношение к предмету.");
        }
    }

    @Override
    public List<BookingDto> getAllByBookerId(Long bookerId, String stateString) {
        List<Booking> result = bookingRepository.findAllByBookerId(bookerId);
        if (result.isEmpty()) {
            log.info("Пользователь id={} не имеет бронирований", bookerId);
            throw new NotFoundException("Бронирований не найдено.");
        } else {
            return filterByState(result, toState(stateString)).stream()
                    .map(BookingMapper::toBookingDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<BookingDto> getAllByOwnerId(Long ownerId, String stateString) {
        List<Booking> result = bookingRepository.getAllByOwnerId(ownerId);
        if (result.isEmpty()) {
            log.info("Пользователь id={} не имеет бронирований", ownerId);
            throw new NotFoundException("Бронирований не найдено.");
        } else {
            return filterByState(result, toState(stateString)).stream()
                    .map(BookingMapper::toBookingDto)
                    .collect(Collectors.toList());
        }
    }

    private State toState(String stateString) {
        try {
            return State.valueOf(stateString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidateException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private List<Booking> filterByState(List<Booking> bookings, State state) {
        switch (state) {
            case ALL:
                return bookings;
            case WAITING:
                return bookings.stream()
                        .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                        .collect(Collectors.toList());
            case REJECTED:
                return bookings.stream()
                        .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                        .collect(Collectors.toList());
            case PAST:
                return bookings.stream()
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case FUTURE:
                return bookings.stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case CURRENT:
                return bookings.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) &&
                                booking.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            default:
                throw new ValidateException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
