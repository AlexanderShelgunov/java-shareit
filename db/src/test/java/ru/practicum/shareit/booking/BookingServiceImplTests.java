package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTests {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;

    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User booker = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(false)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.APPROVED)
            .build();
    private final InputBookingDto bookingInputDto = InputBookingDto.builder()
            .id(booking.getId())
            .start(booking.getStart())
            .end(booking.getEnd())
            .itemId(booking.getItem().getId())
            .build();
    private final Booking notApproveBooking = Booking.builder()
            .id(2L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();

    @Test
    void testAddBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingCreated = bookingService.addBooking(2L, bookingInputDto);
        assertNotEquals(bookingCreated, null);
        assertEquals(booking.getId(), bookingCreated.getId());
        assertEquals(booking.getItem().getId(), bookingCreated.getItem().getId());
        assertEquals(booking.getStart(), bookingCreated.getStart());
        assertEquals(booking.getEnd(), bookingCreated.getEnd());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testAddBookingWhenItemUnknownShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, bookingInputDto));
        assertEquals("Предмет c id=" + bookingInputDto.getItemId() + " не найден.", exception.getMessage());
    }

    @Test
    void testAddBookingWhenBookerUnknownShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, bookingInputDto));
        assertEquals("Пользователь c id=" + 2 + " не найден.", exception.getMessage());
    }

    @Test
    void testAddBookingWhenBookerIsOwnerShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(1L, bookingInputDto));
        assertEquals("Пользователь c id=" + 1 + " является владельцем предмета.", exception.getMessage());
    }

    @Test
    void testAddBookingWhenItemNotAvailableShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(anotherItem));

        ValidateException exception = assertThrows(ValidateException.class,
                () -> bookingService.addBooking(1L, bookingInputDto));
        assertEquals("Неверные параметры бронирования.", exception.getMessage());
    }

    @Test
    void testApproveBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));
        when(bookingRepository.save(any())).thenReturn(notApproveBooking);

        BookingDto bookingApproved = bookingService.approveBooking(1L, 2L, true);
        assertEquals(notApproveBooking.getId(), bookingApproved.getId());
        assertEquals(notApproveBooking.getItem().getId(), bookingApproved.getItem().getId());
        assertEquals(notApproveBooking.getStart(), bookingApproved.getStart());
        assertEquals(notApproveBooking.getEnd(), bookingApproved.getEnd());
        assertEquals(BookingStatus.APPROVED, bookingApproved.getStatus());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testApproveBookingWhenUnknownBookingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование c id=" + bookingInputDto.getId() + "не найдено.", exception.getMessage());
    }

    @Test
    void testApproveBookingByNonOwnerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(2L, 2L, true));
        assertEquals("Пользователь c id=" + 2 + " не является хозяином предмета.", exception.getMessage());
    }

    @Test
    void testApproveBookingWhenStatusIsNotWaitingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ValidateException exception = assertThrows(ValidateException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование не нуждается в изменении статуса.", exception.getMessage());
    }

    @Test
    void testGetBookingByIdByOwner() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto booking = bookingService.getById(1L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void testGetBookingByIdByBooker() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto booking = bookingService.getById(2L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void testGetBookingByIdByNonOwnerOrNonBookerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(3L, 1L));
        assertEquals("Пользователь c id=" + 3 + " не имеет отношение к предмету.", exception.getMessage());
    }

    @Test
    void testGetBookingByIdWithUnknownBookingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Бронирование c id=" + bookingInputDto.getId() + "не найдено.", exception.getMessage());
    }

    @Test
    void testGetAllByBooker() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(bookings.get(1).getStatus(), BookingStatus.WAITING);

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void testGetAllByBookerWithoutBookings() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(Collections.emptyList()));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getAllByBookerId(1L, "ALL", 0, 2));
        assertEquals("Бронирований не найдено.", exception.getMessage());
    }

    @Test
    void testGetAllByBookerWithStateWaiting() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "WAITING", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.WAITING);

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void testGetAllByBookerWithStateRejected() {
        final Booking rejectedBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.minusHours(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, rejectedBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "REJECTED", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    void testGetAllByBookerWithStatePast() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "PAST", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertTrue(bookings.get(0).getEnd().isBefore(date));
        assertTrue(bookings.get(1).getEnd().isBefore(date));

        verify(bookingRepository, times(1)).findAllByBookerId(anyLong(), any());
    }

    @Test
    void testGetAllByBookerWithStateFuture() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "FUTURE", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 0);
    }

    @Test
    void testGetAllByBookerWithStateCurrent() {
        final Booking currentBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, currentBooking)));

        List<BookingDto> bookings = bookingService.getAllByBookerId(2L, "CURRENT", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertTrue(bookings.get(0).getEnd().isAfter(date));
    }

    @Test
    void testGetAllByBookerWithStateUnknownShouldThrowException() {
        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        ValidateException exception = assertThrows(ValidateException.class,
                () -> bookingService.getAllByBookerId(2L, "OLD", 0, 2));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void testGetAllByOwnerId() {
        when(bookingRepository.getAllByOwnerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(List.of(booking, notApproveBooking)));

        List<BookingDto> bookings = bookingService.getAllByOwnerId(1L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(bookings.get(1).getStatus(), BookingStatus.WAITING);

        verify(bookingRepository, times(1)).getAllByOwnerId(anyLong(), any());
    }

    @Test
    void testGetAllByOwnerWithoutItemsShouldThrowException() {
        when(bookingRepository.getAllByOwnerId(anyLong(), any()))
                .thenReturn(new PageImpl<Booking>(Collections.emptyList()));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwnerId(2L, "ALL", 0, 2));
        assertEquals("Бронирований не найдено.", exception.getMessage());
    }
}
