package com.staybook.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.staybook.api.StaybookApiApplication;


import com.staybook.api.dto.HotelMapper;
import com.staybook.api.dto.HotelResponse;
import com.staybook.api.dto.mapper.RoomMapper;
import com.staybook.api.dto.room.RoomResponse;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomStatus;
import com.staybook.api.entity.RoomType;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.service.HotelService;
import com.staybook.api.service.RoomService;

@WebMvcTest(HotelController.class)
public class HotelControllerTest {
    @Autowired
    private MockMvc mockMvc;

     @MockitoBean
    private HotelService hotelService;

     @MockitoBean
    private HotelMapper hotelMapper;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RoomMapper roomMapper;

    @Test
    public void shouldCreateHotelWhenRequestIsValid() throws Exception {
        String requestBody = "{\n" +
                "  \"name\": \"Grand Hotel\",\n" +
                "  \"description\": \"A luxury hotel\",\n" +
                "  \"address\": \"123 Main Street\",\n" +
                "  \"city\": \"Atlanta\",\n" +
                "  \"country\": \"USA\"\n" +
                "}";

        Hotel hotel = Hotel.builder()
            .name("Grand Hotel")
            .description("A luxury hotel")
            .address("123 Main Street")
            .city("Atlanta")
            .country("USA")
            .build();

        HotelResponse response = new HotelResponse(
            1L,
            "Grand Hotel",
            "A luxury hotel",
            "123 Main Street",
            "Atlanta",
            "USA",
            true,
            LocalDateTime.now()
        );

        when(hotelMapper.toEntity(any())).thenReturn(hotel);
        when(hotelService.createHotel(any())).thenReturn(hotel);
        when(hotelMapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/hotels")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Grand Hotel"));
    }
   

    @Test
    public void shouldReturnHotelWhenHotelExists() throws Exception  {
       Hotel hotel = Hotel.builder()
        .name("Grand Hotel")
        .description("A luxury hotel")
        .address("123 Main Street")
        .city("Atlanta")
        .country("USA")
        .build();
    
        HotelResponse response = new HotelResponse(
            1L,
            "Grand Hotel",
            "A luxury hotel",
            "123 Main Street",
            "Atlanta",
            "USA",
            true,
            LocalDateTime.now()
        );

        when(hotelService.getHotelById(1L))
            .thenReturn(hotel);
        when(hotelMapper.toResponse(hotel))
            .thenReturn(response);

        mockMvc.perform(get("/api/hotels/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Grand Hotel"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateHotelRequestIsInvalid() throws Exception {
            String requestBody = """
        {
            "name": "",
            "description": "Invalid hotel",
            "address": "",
            "city": "",
            "country": ""
        }
        """;
        mockMvc.perform(post("/api/hotels")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.name").exists())
        .andExpect(jsonPath("$.errors.address").exists());

        verify(hotelService, never()).createHotel(any());
    }
    
    @Test
    public void shouldReturnNotFoundWhenHotelDoesNotExist() throws Exception {
            when(hotelService.getHotelById(999L))
            .thenThrow(new ResourceNotFoundException("Hotel not found"));

            mockMvc.perform(get("/api/hotels/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Hotel not found"));

            verify(hotelService).getHotelById(999L);
    }
    
    @Test
     void shouldReturnAllHotels() throws Exception {
        Hotel hotel1 = Hotel.builder()
            .name("Grand Hotel")
            .description("A luxury hotel")
            .address("123 Main Street")
            .city("Atlanta")
            .country("USA")
            .build();

        Hotel hotel2 = Hotel.builder()
            .name("Ocean View Resort")
            .description("A beachfront resort")
            .address("456 Ocean Drive")
            .city("Miami")
            .country("USA")
            .build();

        when(hotelService.getAllHotels()).thenReturn(List.of(hotel1, hotel2));

        HotelResponse response1 = new HotelResponse(
            1L,
            "Grand Hotel",
            "A luxury hotel",
            "123 Main Street",
            "Atlanta",
            "USA",
            true,
            LocalDateTime.now()
        );

        HotelResponse response2 = new HotelResponse(
            2L,
            "Ocean View Resort",
            "A beachfront resort",
            "456 Ocean Drive",
            "Miami",
            "USA",
            true,
            LocalDateTime.now()
        );

        when(hotelMapper.toResponse(hotel1)).thenReturn(response1);
        when(hotelMapper.toResponse(hotel2)).thenReturn(response2);

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Grand Hotel"))
                .andExpect(jsonPath("$[1].name").value("Ocean View Resort"));

        verify(hotelService).getAllHotels();
        verify(hotelMapper).toResponse(hotel1);
        verify(hotelMapper).toResponse(hotel2);
     }

    @Test
    void shouldReturnEmptyListWhenNoHotelsExist() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(List.of());

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(hotelService).getAllHotels();
        verify(hotelMapper, never()).toResponse(any());

    } 

    @Test
    void shouldReturnRoomsForHotel() throws Exception {
        Room room1 = Room.builder()
        .id(1L)
        .roomNumber("101")
        .roomType(RoomType.DOUBLE)
        .pricePerNight(new BigDecimal("10.25"))
        .capacity(2)
        .status(RoomStatus.AVAILABLE)
        .build();

        Room room2 = Room.builder()
            .id(2L)
            .roomNumber("102")
            .roomType(RoomType.SINGLE)
            .pricePerNight(new BigDecimal("8.50"))
            .capacity(1)
            .status(RoomStatus.AVAILABLE)
            .build();

        RoomResponse response1 = new RoomResponse(
        1L,
        "101",
        RoomType.DOUBLE,
        new BigDecimal("10.25"),
        2,
        RoomStatus.AVAILABLE
);

        RoomResponse response2 = new RoomResponse(
                2L,
                "102",
                RoomType.SINGLE,
                new BigDecimal("8.50"),
                1,
                RoomStatus.AVAILABLE
        );
        
        when(roomService.getRoomsByHotel(1L))
        .thenReturn(List.of(room1, room2));

        when(roomMapper.toResponse(room1))
                .thenReturn(response1);

        when(roomMapper.toResponse(room2))
                .thenReturn(response2);

        mockMvc.perform(get("/api/hotels/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomNumber").value("101"))
                .andExpect(jsonPath("$[1].roomNumber").value("102"));

        verify(roomService).getRoomsByHotel(1L);
        verify(roomMapper).toResponse(room1);
        verify(roomMapper).toResponse(room2);
    }

    @Test
    void shouldReturnEmptyListWhenHotelHasNoRooms() throws Exception {

        when(roomService.getRoomsByHotel(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/hotels/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(roomService).getRoomsByHotel(1L);
        verifyNoInteractions(roomMapper);
    }

    @Test
    void shouldReturnNotFoundWhenHotelDoesNotExistForRoomSearch() throws Exception {

        when(roomService.getRoomsByHotel(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Hotel not found with id: 999"
                ));

        mockMvc.perform(get("/api/hotels/999/rooms"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Hotel not found with id: 999"));

        verify(roomService).getRoomsByHotel(999L);
        verifyNoInteractions(roomMapper);
    }

}
