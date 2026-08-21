package com.staybook.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
