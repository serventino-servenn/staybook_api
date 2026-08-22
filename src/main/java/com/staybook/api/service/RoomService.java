package com.staybook.api.service;


import com.staybook.api.entity.Room;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }
}
