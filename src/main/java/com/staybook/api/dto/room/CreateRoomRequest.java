package com.staybook.api.dto.room;

import java.math.BigDecimal;

import com.staybook.api.entity.RoomType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRoomRequest(
    @NotNull
    Long hotelId,

    @NotBlank
    String roomNumber,

    @NotNull
    RoomType roomType,

    @NotNull
    @Positive
    BigDecimal pricePerNight,
    
    @NotNull
    @Positive
    Integer capacity
) {
    
}
