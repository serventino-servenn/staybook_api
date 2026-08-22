package com.staybook.api.service;

import com.staybook.api.entity.Hotel;
import com.staybook.api.exception.ResourceNotFoundException;
import com.staybook.api.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        hotelService = new HotelService(hotelRepository);
    }

    @Test
    void shouldCreateHotel() {
        Hotel hotel = Hotel.builder()
                .name("Grand Stay Hotel")
                .description("A comfortable hotel")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .build();

        when(hotelRepository.save(hotel)).thenReturn(hotel);

        Hotel result = hotelService.createHotel(hotel);

        assertThat(result).isEqualTo(hotel);

        verify(hotelRepository).save(hotel);
    }

    @Test
    void shouldGetHotelById() {
        Long hotelId = 1L;

        Hotel hotel = Hotel.builder()
                .id(hotelId)
                .name("Grand Stay Hotel")
                .description("A comfortable hotel")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .build();

        when(hotelRepository.findById(hotelId))
                .thenReturn(Optional.of(hotel));

        Hotel result = hotelService.getHotelById(hotelId);

        assertThat(result).isEqualTo(hotel);

        verify(hotelRepository).findById(hotelId);
    }

    @Test
    void shouldGetAllHotels() {
        Hotel hotel1 = Hotel.builder()
                .id(1L)
                .name("Grand Stay Hotel")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .build();

        Hotel hotel2 = Hotel.builder()
                .id(2L)
                .name("City View Hotel")
                .address("456 Peachtree Street")
                .city("Atlanta")
                .country("USA")
                .build();

        when(hotelRepository.findAll())
                .thenReturn(List.of(hotel1, hotel2));

        List<Hotel> result = hotelService.getAllHotels();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(hotel1, hotel2);

        verify(hotelRepository).findAll();
    }

    @Test
    void shouldThrowExceptionWhenHotelDoesNotExist() {
        Long hotelId = 999L;

        when(hotelRepository.findById(hotelId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById(hotelId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 999");

        verify(hotelRepository).findById(hotelId);
    }
}
