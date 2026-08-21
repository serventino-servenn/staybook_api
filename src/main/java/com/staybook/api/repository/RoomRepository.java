package com.staybook.api.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}