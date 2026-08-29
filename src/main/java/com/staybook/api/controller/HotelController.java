package com.staybook.api.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staybook.api.dto.CreateHotelRequest;
import com.staybook.api.dto.HotelMapper;
import com.staybook.api.dto.HotelResponse;
import com.staybook.api.entity.Hotel;
import com.staybook.api.service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelMapper hotelMapper;
    
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
    
}
