package com.staybook.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
