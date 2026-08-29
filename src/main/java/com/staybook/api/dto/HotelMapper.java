package com.staybook.api.dto;

import org.springframework.stereotype.Component;
import com.staybook.api.entity.Hotel;

@Component
public class HotelMapper {
    public HotelResponse toResponse(Hotel hotel){
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.isActive(),
               hotel.getCreatedAt()
        );
    }

  public Hotel toEntity(CreateHotelRequest request) {
        return Hotel.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .country(request.country())
                .build();
   }
}
