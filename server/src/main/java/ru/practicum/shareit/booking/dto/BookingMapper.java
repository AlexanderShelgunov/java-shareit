package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public static Booking fromInputBookingDto(InputBookingDto inputBookingDto, Item item, User booker) {
        return Booking.builder()
                .id(inputBookingDto.getId())
                .start(inputBookingDto.getStart())
                .end(inputBookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    public static ItemOutputDto.BookingDtoForItem toBookingDtoForItem(Booking booking) {
        if (booking != null) {
            return ItemOutputDto.BookingDtoForItem.builder()
                    .id(booking.getId())
                    .bookerId(booking.getBooker().getId())
                    .build();
        } else return null;
    }
}