package com.staybook.api.service;

import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
    }

    @Test
    void shouldCreateRoom() {
        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Grand Stay Hotel")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .build();

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .hotel(hotel)
                .build();

        when(roomRepository.save(room)).thenReturn(room);

        Room result = roomService.createRoom(room);

        assertThat(result).isEqualTo(room);
        verify(roomRepository).save(room);
    }

    @Test
    void shouldGetRoomById() {
        Long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .build();

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(roomId);

        assertThat(result).isEqualTo(room);
        verify(roomRepository).findById(roomId);
    }

    @Test
    void shouldThrowExceptionWhenRoomDoesNotExist() {
        Long roomId = 999L;

        when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found with id: 999");

        verify(roomRepository).findById(roomId);
    }

    @Test
    void shouldGetRoomsByHotel() {
        Long hotelId = 1L;

        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Grand Stay Hotel")
                .build();

        Room room1 = Room.builder()
                .id(1L)
                .roomNumber("101")
                .hotel(hotel)
                .build();

        Room room2 = Room.builder()
                .id(2L)
                .roomNumber("102")
                .hotel(hotel)
                .build();

        when(roomRepository.findByHotelId(hotelId))
                .thenReturn(List.of(room1, room2));

        List<Room> result = roomService.getRoomsByHotel(hotelId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(room1, room2);

        verify(roomRepository).findByHotelId(hotelId);
    }
}
