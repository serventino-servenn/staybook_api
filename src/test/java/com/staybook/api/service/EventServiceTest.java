package com.staybook.api.service;


import com.staybook.api.dto.event.CreateEventRequest;
import com.staybook.api.dto.mapper.EventMapper;
import com.staybook.api.entity.Event;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventService eventService;
    
    @Mock
    private EventMapper eventMapper;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, eventMapper);
    }

    @Test
    void shouldCreateEvent() {
        Event event = Event.builder()
                .id(1L)
                .name("Summer Music Festival")
                .description("Annual summer music event")
                .venue("Downtown Arena")
                .build();

        CreateEventRequest request = new CreateEventRequest(
                event.getName(),
                event.getDescription(),
                event.getVenue(),
                event.getEventDate(),
                event.getCapacity(),
                event.getPrice()
        );

        when(eventMapper.toEntity(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventService.createEvent(request);

        assertThat(result).isEqualTo(event);
        assertThat(event.getAvailableSeats()).isEqualTo(request.capacity());
        verify(eventMapper).toEntity(request);
        verify(eventRepository).save(event);
    }

    @Test
    void shouldGetEventById() {
        Long eventId = 1L;

        Event event = Event.builder()
                .id(eventId)
                .name("Summer Music Festival")
                .venue("Downtown Arena")
                .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        Event result = eventService.getEventById(eventId);

        assertThat(result).isEqualTo(event);
        verify(eventRepository).findById(eventId);
    }

    @Test
    void shouldThrowExceptionWhenEventDoesNotExist() {
        Long eventId = 999L;

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found with id: 999");

        verify(eventRepository).findById(eventId);
    }

    @Test
    void shouldGetAllEvents() {
        Event event1 = Event.builder()
                .id(1L)
                .name("Summer Music Festival")
                .venue("Downtown Arena")
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .name("Tech Conference")
                .venue("Convention Center")
                .build();

        when(eventRepository.findAll())
                .thenReturn(List.of(event1, event2));

        List<Event> result = eventService.getAllEvents();

        assertThat(result)
                .hasSize(2)
                .containsExactly(event1, event2);

        verify(eventRepository).findAll();
    }
}
