package com.staybook.api.service;

import com.staybook.api.dto.room.CreateRoomRequest;
import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomType;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

       @Mock
        private RoomRepository roomRepository;

        @Mock
        private HotelRepository hotelRepository;

        private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, hotelRepository);
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

        CreateRoomRequest request = new CreateRoomRequest(
                1L,
                "101",
                RoomType.DOUBLE,
                new BigDecimal("10.25"),
                2
        );

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("10.25"))
                .capacity(2)
                .hotel(hotel)
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        when(roomRepository.save(any(Room.class)))
                .thenReturn(room);

        Room result = roomService.createRoom(request);

        assertThat(result).isEqualTo(room);

        verify(hotelRepository).findById(1L);
        verify(roomRepository).save(any(Room.class));
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

        when(hotelRepository.findById(1L))
        .thenReturn(Optional.of(hotel));

        when(roomRepository.findByHotelId(1L))
        .thenReturn(List.of(room1, room2));

        List<Room> result = roomService.getRoomsByHotel(1L);

        assertThat(result)
                .hasSize(2)
                .containsExactly(room1, room2);

        verify(hotelRepository).findById(1L);
        verify(roomRepository).findByHotelId(1L);
    }

    @Test
     void shouldThrowExceptionWhenHotelDoesNotExist() {

        CreateRoomRequest request = new CreateRoomRequest(
                999L,
                "101",
                RoomType.DOUBLE,
                new BigDecimal("10.25"),
                2
        );

        when(hotelRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 999");

        verify(hotelRepository).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
     }

     @Test
      void shouldThrowExceptionWhenHotelDoesNotExistForRoomSearch() {

        when(hotelRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomsByHotel(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 999");

        verify(hotelRepository).findById(999L);
        verify(roomRepository, never()).findByHotelId(999L);
     }
}
