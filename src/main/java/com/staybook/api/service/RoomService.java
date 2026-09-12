package com.staybook.api.service;


import com.staybook.api.dto.mapper.RoomMapper;
import com.staybook.api.dto.room.CreateRoomRequest;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;;

    public Room createRoom(CreateRoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Hotel not found with id: " + request.hotelId()
                ));
        Room room = Room.builder()
        .roomNumber(request.roomNumber())
        .roomType(request.roomType())
        .pricePerNight(request.pricePerNight())
        .capacity(request.capacity())
        .hotel(hotel)
        .build();

        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByHotel(Long hotelId) {

        hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hotel not found with id: " + hotelId
                        ));

        return roomRepository.findByHotelId(hotelId);
    }
}
