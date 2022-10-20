package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.InputBookingDto;

import java.util.List;

@Service
public interface BookingService {

    BookingDto addBooking(Long bookerId, InputBookingDto inputBookingDto);

    BookingDto approveBooking(Long ownerId, Long bookingId, Boolean isApprove);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllByBookerId(Long bookerId, String state);

    List<BookingDto> getAllByOwnerId(Long ownerId, String state);
}
