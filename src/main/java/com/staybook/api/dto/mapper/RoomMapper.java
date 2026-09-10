package com.staybook.api.dto.mapper;

import org.springframework.stereotype.Component;

import com.staybook.api.dto.room.CreateRoomRequest;
import com.staybook.api.dto.room.RoomResponse;
import com.staybook.api.entity.Room;

@Component 
public class RoomMapper {
    public Room toEntity(CreateRoomRequest request) {
        return Room.builder()
                .roomNumber(request.roomNumber())
                .roomType(request.roomType())
                .pricePerNight(request.pricePerNight())
                .capacity(request.capacity())
                .build();
                
    }

    public RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getCapacity(),
                room.getStatus()
        );
    }
}
