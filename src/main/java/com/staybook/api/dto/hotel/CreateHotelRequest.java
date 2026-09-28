package com.staybook.api.dto.hotel;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(
     @NotBlank
    String name,
    String description,
    @NotBlank
    String address,
    @NotBlank
    String city,
    @NotBlank
    String country
) {
    
}
