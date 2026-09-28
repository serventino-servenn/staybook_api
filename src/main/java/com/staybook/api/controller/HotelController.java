package com.staybook.api.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staybook.api.dto.hotel.CreateHotelRequest;
import com.staybook.api.dto.hotel.HotelResponse;
import com.staybook.api.dto.mapper.HotelMapper;
import com.staybook.api.dto.mapper.RoomMapper;
import com.staybook.api.dto.room.RoomResponse;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.service.HotelService;
import com.staybook.api.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final HotelMapper hotelMapper;
    private final RoomMapper roomMapper;
    
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long hotelId) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        HotelResponse response = hotelMapper.toResponse(hotel);
            return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(
        
        @Valid @RequestBody CreateHotelRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel savedHotel = hotelService.createHotel(hotel);
        HotelResponse response = hotelMapper.toResponse(savedHotel);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        List<HotelResponse> responses = hotels.stream()
                .map(hotelMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

     @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotel(
            @PathVariable Long hotelId) {

        List<Room> rooms = roomService.getRoomsByHotel(hotelId);

        List<RoomResponse> responses = rooms.stream()
                .map(roomMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
