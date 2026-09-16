package com.staybook.api.controller;


import com.staybook.api.dto.event.CreateEventRequest;
import com.staybook.api.dto.event.EventResponse;
import com.staybook.api.dto.mapper.EventMapper;
import com.staybook.api.entity.Event;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.exception.ResourceNotFoundException;

import com.staybook.api.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventMapper eventMapper;

    @Test
    void shouldCreateEvent() throws Exception {

        CreateEventRequest request = new CreateEventRequest(
                "Java Conference",
                "A conference about Java and Spring Boot",
                "Atlanta Convention Center",
                LocalDateTime.of(2026, 10, 15, 18, 0),
                200,
                new BigDecimal("49.99")
        );

        Event event = Event.builder()
                .id(1L)
                .name("Java Conference")
                .description("A conference about Java and Spring Boot")
                .venue("Atlanta Convention Center")
                .eventDate(request.eventDate())
                .capacity(200)
                .availableSeats(200)
                .price(new BigDecimal("49.99"))
                .status(EventStatus.UPCOMING)
                .build();

        EventResponse response = new EventResponse(
                1L,
                "Java Conference",
                "A conference about Java and Spring Boot",
                "Atlanta Convention Center",
                request.eventDate(),
                200,
                200,
                new BigDecimal("49.99"),
                EventStatus.UPCOMING,
                LocalDateTime.of(2026, 9, 14, 10, 0)
        );

        when(eventService.createEvent(any(CreateEventRequest.class)))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Conference"))
                .andExpect(jsonPath("$.venue").value("Atlanta Convention Center"))
                .andExpect(jsonPath("$.capacity").value(200))
                .andExpect(jsonPath("$.availableSeats").value(200))
                .andExpect(jsonPath("$.price").value(49.99))
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    @Test
    void shouldRejectInvalidCreateEventRequest() throws Exception {

        CreateEventRequest request = new CreateEventRequest(
                "",
                "Description",
                "",
                null,
                0,
                new BigDecimal("-10.00")
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetEventById() throws Exception {

        Event event = Event.builder()
                .id(1L)
                .name("Java Conference")
                .description("A conference about Java and Spring Boot")
                .venue("Atlanta Convention Center")
                .eventDate(LocalDateTime.of(2026, 10, 15, 18, 0))
                .capacity(200)
                .availableSeats(200)
                .price(new BigDecimal("49.99"))
                .status(EventStatus.UPCOMING)
                .build();

        EventResponse response = new EventResponse(
                1L,
                "Java Conference",
                "A conference about Java and Spring Boot",
                "Atlanta Convention Center",
                LocalDateTime.of(2026, 10, 15, 18, 0),
                200,
                200,
                new BigDecimal("49.99"),
                EventStatus.UPCOMING,
                LocalDateTime.of(2026, 9, 14, 10, 0)
        );

        when(eventService.getEventById(1L))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Conference"))
                .andExpect(jsonPath("$.venue").value("Atlanta Convention Center"))
                .andExpect(jsonPath("$.capacity").value(200))
                .andExpect(jsonPath("$.availableSeats").value(200))
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {

        when(eventService.getEventById(999L))
                .thenThrow(new ResourceNotFoundException("Event not found"));

        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllEvents() throws Exception {

        Event event1 = Event.builder()
                .id(1L)
                .name("Java Conference")
                .description("Java conference")
                .venue("Atlanta Convention Center")
                .eventDate(LocalDateTime.of(2026, 10, 15, 18, 0))
                .capacity(200)
                .availableSeats(200)
                .price(new BigDecimal("49.99"))
                .status(EventStatus.UPCOMING)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .name("Spring Boot Meetup")
                .description("Spring Boot meetup")
                .venue("Tech Center")
                .eventDate(LocalDateTime.of(2026, 11, 20, 18, 0))
                .capacity(100)
                .availableSeats(100)
                .price(new BigDecimal("20.00"))
                .status(EventStatus.UPCOMING)
                .build();

        EventResponse response1 = new EventResponse(
                1L,
                "Java Conference",
                "Java conference",
                "Atlanta Convention Center",
                event1.getEventDate(),
                200,
                200,
                new BigDecimal("49.99"),
                EventStatus.UPCOMING,
                LocalDateTime.of(2026, 9, 14, 10, 0)
        );

        EventResponse response2 = new EventResponse(
                2L,
                "Spring Boot Meetup",
                "Spring Boot meetup",
                "Tech Center",
                event2.getEventDate(),
                100,
                100,
                new BigDecimal("20.00"),
                EventStatus.UPCOMING,
                LocalDateTime.of(2026, 9, 14, 10, 0)
        );

        when(eventService.getAllEvents())
                .thenReturn(List.of(event1, event2));

        when(eventMapper.toResponse(event1))
                .thenReturn(response1);

        when(eventMapper.toResponse(event2))
                .thenReturn(response2);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Conference"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Spring Boot Meetup"));
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsExist() throws Exception {

        when(eventService.getAllEvents())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}