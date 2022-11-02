package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

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
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(date.plusDays(1))
            .end(date.plusDays(1))
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
    void testAddBooking() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(createContentFromInputBookingDto(post("/bookings"), bookingInputDto, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void testAddBookingWithInvalidBookingShouldThrowException() throws Exception {
        final InputBookingDto invalidBooking = InputBookingDto.builder()
                .id(booking.getId())
                .start(date.minusDays(1))
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .build();
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(createContentFromInputBookingDto(post("/bookings"), invalidBooking, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testApproveBooking() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void testApproveBookingWithUnknownBooking() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenThrow(new NotFoundException(""));

        mvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testApproveBookingWithoutUserIdHeader() throws Exception {
        when(bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(patch("/bookings/" + booking.getId())
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetById() throws Exception {
        when(bookingService.getById(owner.getId(), booking.getId()))
                .thenReturn(BookingMapper.toBookingDto(booking));

        mvc.perform(get("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void testGetAllByBookerId() throws Exception {
        when(bookingService.getAllByBookerId(booker.getId(), "ALL", 0, 10))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));

        mvc.perform(createRequestWithPagination(get("/bookings"),
                        booker.getId(),
                        "ALL",
                        "0",
                        "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void testGetAllByBookerIdWithoutParam() throws Exception {
        when(bookingService.getAllByBookerId(booker.getId(), "ALL", 0, 20))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void testGetAllByBookerIdWithWrongParam() throws Exception {
            mvc.perform(createRequestWithPagination(get("/bookings"),
                    booker.getId(),
                    "ALL",
                    "-7",
                    "10"))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllByOwnerId() throws Exception {
        when(bookingService.getAllByOwnerId(owner.getId(), "ALL", 0, 10))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));

        mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                        owner.getId(),
                        "ALL",
                        "0",
                        "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    @Test
    void testGetAllByOwnerIdWithWrongParam() throws Exception {
            mvc.perform(createRequestWithPagination(get("/bookings/owner"),
                    owner.getId(),
                    "ALL",
                    "-5",
                    "-1"))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllByOwnerIdWithoutParam() throws Exception {
        when(bookingService.getAllByOwnerId(owner.getId(), "ALL", 0, 20))
                .thenReturn(List.of(BookingMapper.toBookingDto(booking), BookingMapper.toBookingDto(notApproveBooking)));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[1].status", is(notApproveBooking.getStatus().toString())));
    }

    private MockHttpServletRequestBuilder createContentFromInputBookingDto(MockHttpServletRequestBuilder builder,
                                                                           InputBookingDto inputBookingDto,
                                                                           Long id) throws JsonProcessingException {
        return builder
                .content(mapper.writeValueAsString(inputBookingDto))
                .header("X-Sharer-User-Id", id)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createRequestWithPagination(MockHttpServletRequestBuilder builder,
                                                                      Long id,
                                                                      String state,
                                                                      String from,
                                                                      String size) throws JsonProcessingException {
        return builder
                .header("X-Sharer-User-Id", id)
                .param("state", state)
                .param("from", from)
                .param("size", size);
    }
}