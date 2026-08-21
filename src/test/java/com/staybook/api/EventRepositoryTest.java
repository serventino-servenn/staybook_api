package com.staybook.api;


import com.staybook.api.entity.Event;
import com.staybook.api.entity.EventStatus;
import com.staybook.api.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndFindEvent() {
        Event event = Event.builder()
                .name("Summer Music Festival")
                .description("Annual outdoor music festival")
                .venue("Central Park")
                .eventDate(LocalDateTime.now().plusDays(30))
                .capacity(500)
                .availableSeats(500)
                .price(new BigDecimal("75.00"))
                .build();

        Event savedEvent = eventRepository.save(event);

        var result = eventRepository.findById(savedEvent.getId());

        assertThat(result).isPresent();

        Event saved = result.get();

        assertThat(saved.getName()).isEqualTo("Summer Music Festival");
        assertThat(saved.getCapacity()).isEqualTo(500);
        assertThat(saved.getAvailableSeats()).isEqualTo(500);
        assertThat(saved.getStatus()).isEqualTo(EventStatus.UPCOMING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
