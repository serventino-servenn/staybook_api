package com.staybook.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

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
import com.staybook.api.entity.Hotel;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.service.HotelService;

@WebMvcTest(HotelController.class)
public class HotelControllerTest {
    @Autowired
    private MockMvc mockMvc;

     @MockitoBean
    private HotelService hotelService;

     @MockitoBean
    private HotelMapper hotelMapper;

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
    

}
