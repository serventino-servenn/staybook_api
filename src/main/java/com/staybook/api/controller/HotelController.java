package com.staybook.api.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staybook.api.dto.HotelMapper;
import com.staybook.api.dto.HotelResponse;
import com.staybook.api.entity.Hotel;
import com.staybook.api.service.HotelService;

import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/hotels")
// @RequiredArgsConstructor
// public class HotelController {

//     private final HotelService hotelService;
//     private final HotelMapper hotelMapper;
    
//     @GetMapping("/{hotelId}")
//     public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long hotelId) {
//         Hotel hotel = hotelService.getHotelById(hotelId);
//         HotelResponse response = hotelMapper.toResponse(hotel);
//             return ResponseEntity.ok(response);
//     }
// }
