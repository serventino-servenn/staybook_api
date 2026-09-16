package com.staybook.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.staybook.api.dto.event.CreateEventRequest;
import com.staybook.api.dto.event.EventResponse;
import com.staybook.api.dto.mapper.EventMapper;
import com.staybook.api.entity.Event;
import com.staybook.api.service.EventService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        Event event = eventService.createEvent(request);
        EventResponse response = eventMapper.toResponse(event);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


   @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long eventId) {

        Event event = eventService.getEventById(eventId);
        EventResponse response = eventMapper.toResponse(event);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventResponse> responses = events.stream()
                .map(eventMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);        
    }
    
    
    
}
