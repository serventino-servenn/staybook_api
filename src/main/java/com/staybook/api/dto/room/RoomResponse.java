package com.staybook.api.dto.room;

import java.math.BigDecimal;

import com.staybook.api.entity.RoomStatus;
import com.staybook.api.entity.RoomType;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType roomType,
        BigDecimal pricePerNight,
        Integer capacity,
        RoomStatus status
) {
    
}
