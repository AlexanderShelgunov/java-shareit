package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestsControllerTests {
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final User requester = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(2L)
            .description("itemRequestDescription")
            .requestor(requester)
            .created(LocalDateTime.now())
            .build();

    @Test
    void testCreateRequest() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    void testCreateRequestWithoutUserIdHeader() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.createRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateRequestWithInvalidInputRequest() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(null))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetRequestById() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        when(itemRequestService.findRequestById(anyLong(), anyLong()))
                .thenReturn(ItemRequestMapper.itemRequestOutputDto(itemRequest, Collections.emptyList()));

        mvc.perform(get("/requests/" + itemRequestDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class));
    }

    @Test
    void testGetRequestByIdWithoutUserIdHeader() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        mvc.perform(get("/requests/" + itemRequestDto.getId()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllRequestsFromRequester() throws Exception {
        when(itemRequestService.findAllRequestFromRequester(anyLong()))
                .thenReturn(List.of(ItemRequestMapper.itemRequestOutputDto(itemRequest, Collections.emptyList())));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    @Test
    void testGetAllRequestsFromRequesterWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllRequests() throws Exception {
        when(itemRequestService.findAllRequest(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(ItemRequestMapper.itemRequestOutputDto(itemRequest,Collections.emptyList())));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    @Test
    void testGetAllRequestsWithoutParam() throws Exception {
        when(itemRequestService.findAllRequest(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(ItemRequestMapper.itemRequestOutputDto(itemRequest, Collections.emptyList())));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())));
    }

    @Test
    void testGetAllRequestsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isInternalServerError());
    }
}
