package com.staybook.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.staybook.api.dto.mapper.RoomMapper;
import com.staybook.api.dto.room.CreateRoomRequest;
import com.staybook.api.dto.room.RoomResponse;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomStatus;
import com.staybook.api.entity.RoomType;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.service.RoomService;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private RoomService roomService;

    @MockitoBean 
    private RoomMapper roomMapper;

    @Test
    void shouldReturnRoomWhenRoomExists() throws Exception {

        // Arrange
        Hotel mockHotel = new Hotel();

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("10.25"))
                .capacity(2)
                .hotel(mockHotel)
                .build();

        RoomResponse mockResponse = new RoomResponse(
                1L,
                "101",
                RoomType.DOUBLE,
                new BigDecimal("10.25"),
                2,
                RoomStatus.AVAILABLE
        );

        when(roomService.getRoomById(1L)).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(mockResponse);

      
        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.roomType").value("DOUBLE"))
                .andExpect(jsonPath("$.pricePerNight").value(10.25))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(roomService).getRoomById(1L);
        verify(roomMapper).toResponse(room);
    }

    @Test
        void shouldReturnNotFoundWhenRoomDoesNotExist() throws Exception {

        when(roomService.getRoomById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Room not found with id: 999"
                ));

        mockMvc.perform(get("/api/rooms/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Room not found with id: 999"));

        verify(roomService).getRoomById(999L);
     }

     @Test
     void shouldReturnBadRequestWhenCreateRoomRequestIsInvalid() throws Exception {

        String requestBody = """
                {
                "hotelId": null,
                "roomNumber": "",
                "roomType": null,
                "pricePerNight": -10,
                "capacity": 0
                }
                """;

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(roomService, never()).createRoom(any(CreateRoomRequest.class));
     }

     @Test
     void shouldReturnNotFoundWhenHotelDoesNotExist() throws Exception {

        String requestBody = """
                {
                "hotelId": 999,
                "roomNumber": "101",
                "roomType": "DOUBLE",
                "pricePerNight": 10.25,
                "capacity": 2
                }
                """;

        when(roomService.createRoom(any(CreateRoomRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Hotel not found with id: 999"
                ));
        

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Hotel not found with id: 999"));

        verify(roomService).createRoom(any(CreateRoomRequest.class));
      }

      

     
   
}
