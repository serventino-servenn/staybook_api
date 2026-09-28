package com.staybook.api;

import com.staybook.api.entity.Hotel;
import com.staybook.api.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void shouldSaveAndFindHotel() {
        Hotel hotel = Hotel.builder()
                .name("Grand Stay Hotel")
                .description("A comfortable hotel in the city center")
                .address("123 Main Street")
                .city("Atlanta")
                .country("USA")
                .active(true)
                .build();

        Hotel savedHotel = hotelRepository.save(hotel);

        var result = hotelRepository.findById(savedHotel.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Grand Stay Hotel");
        assertThat(result.get().getCity()).isEqualTo("Atlanta");
    }
}
