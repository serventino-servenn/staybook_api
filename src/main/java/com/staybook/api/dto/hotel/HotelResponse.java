package com.staybook.api.dto.hotel;

import java.time.LocalDateTime;

public record HotelResponse(
    Long id,
    String name,
    String description,
    String address,
    String city,
    String country,
    boolean active,
    LocalDateTime createdAt
) {
    
}
