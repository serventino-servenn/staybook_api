package com.staybook.api.dto;

public record CreateHotelRequest(
    String name,
    String description,
    String address,
    String city,
    String country
) {
    
}
