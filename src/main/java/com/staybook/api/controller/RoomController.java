package com.staybook.api.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staybook.api.dto.mapper.RoomMapper;
import com.staybook.api.dto.room.CreateRoomRequest;
import com.staybook.api.dto.room.RoomResponse;
import com.staybook.api.entity.Room;
import com.staybook.api.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        Room room = roomService.getRoomById(roomId);
        RoomResponse response = roomMapper.toResponse(room);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
        @Valid @RequestBody CreateRoomRequest request) {
        Room room = roomService.createRoom(request);
        RoomResponse response = roomMapper.toResponse(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
   
}
