package com.staybook.api;


import com.staybook.api.entity.Hotel;
import com.staybook.api.entity.Room;
import com.staybook.api.entity.RoomType;
import com.staybook.api.repository.HotelRepository;
import com.staybook.api.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void shouldSaveRoomWithHotel() {
        Hotel hotel = createHotel("Grand Stay Hotel");
        hotelRepository.save(hotel);

        Room room = createRoom("101", hotel);

        Room savedRoom = roomRepository.save(room);

        assertThat(savedRoom.getId()).isNotNull();
        assertThat(savedRoom.getRoomNumber()).isEqualTo("101");
        assertThat(savedRoom.getHotel().getId()).isEqualTo(hotel.getId());
    }

    @Test
    void shouldNotAllowDuplicateRoomNumberWithinSameHotel() {
        Hotel hotel = createHotel("Grand Stay Hotel");
        hotelRepository.save(hotel);

        roomRepository.saveAndFlush(createRoom("101", hotel));

        Room duplicateRoom = createRoom("101", hotel);

        assertThatThrownBy(() -> roomRepository.saveAndFlush(duplicateRoom))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowSameRoomNumberInDifferentHotels() {
        Hotel firstHotel = createHotel("Grand Stay Hotel");
        Hotel secondHotel = createHotel("City View Hotel");

        hotelRepository.save(firstHotel);
        hotelRepository.save(secondHotel);

        Room firstRoom = roomRepository.saveAndFlush(
                createRoom("101", firstHotel)
        );

        Room secondRoom = roomRepository.saveAndFlush(
                createRoom("101", secondHotel)
        );

        assertThat(firstRoom.getId()).isNotEqualTo(secondRoom.getId());
    }

    private Hotel createHotel(String name) {
        return Hotel.builder()
                .name(name)
                .description("Test hotel")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .build();
    }

    private Room createRoom(String roomNumber, Hotel hotel) {
        return Room.builder()
                .roomNumber(roomNumber)
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(2)
                .hotel(hotel)
                .build();
    }
}
