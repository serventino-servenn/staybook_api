
package com.staybook.api.controller;

import com.staybook.api.dto.mapper.ReservationMapper;
import com.staybook.api.security.CustomUserDetailsService;
import com.staybook.api.security.JwtService;
import com.staybook.api.service.ReservationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import com.staybook.api.controller.ReservationController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.staybook.api.dto.reservation.CreateHotelReservationRequest;
import com.staybook.api.dto.reservation.ReservationResponse;
import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;
import org.springframework.context.annotation.Import;
import com.staybook.api.security.SecurityConfig;
import com.staybook.api.dto.reservation.CreateEventReservationRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationMapper reservationMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldCreateHotelReservation() throws Exception {

        String email = "customer@example.com";

        CreateHotelReservationRequest request = new CreateHotelReservationRequest(
                10L,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 1, 15));

        Reservation reservation = Reservation.builder()
                .id(1L)
                .type(ReservationType.HOTEL)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.of(2026, 10, 29, 10, 0))
                .build();

        ReservationResponse response = new ReservationResponse(
                1L,
                5L,
                10L,
                null,
                ReservationType.HOTEL,
                request.checkIn(),
                request.checkOut(),
                reservation.getReservedAt(),
                ReservationStatus.CONFIRMED);

        when(reservationService.createHotelReservation(
                any(CreateHotelReservationRequest.class), eq(email)))
                .thenReturn(reservation);

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        mockMvc.perform(post("/api/reservations/hotel")
                .with(user(email).roles("CUSTOMER"))
                .contentType(APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.roomId").value(10))
                .andExpect(jsonPath("$.eventId").doesNotExist())
                .andExpect(jsonPath("$.type").value("HOTEL"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).createHotelReservation(
                any(CreateHotelReservationRequest.class), eq(email));
    }

    @Test
    void shouldCreateEventReservation() throws Exception {

        String email = "customer@example.com";

        CreateEventReservationRequest request = new CreateEventReservationRequest(20L);

        Reservation reservation = Reservation.builder()
                .id(2L)
                .type(ReservationType.EVENT)
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.of(2026, 10, 29, 11, 0))
                .build();

        ReservationResponse response = new ReservationResponse(
                2L,
                5L,
                null,
                20L,
                ReservationType.EVENT,
                null,
                null,
                reservation.getReservedAt(),
                ReservationStatus.CONFIRMED);

        when(reservationService.createEventReservation(
                any(CreateEventReservationRequest.class), eq(email)))
                .thenReturn(reservation);

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        mockMvc.perform(post("/api/reservations/event")
                .with(user(email).roles("CUSTOMER"))
                .contentType(APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.roomId").doesNotExist())
                .andExpect(jsonPath("$.eventId").value(20))
                .andExpect(jsonPath("$.type").value("EVENT"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).createEventReservation(
                any(CreateEventReservationRequest.class), eq(email));
    }

    @Test
    void shouldGetCustomerReservations() throws Exception {

        String email = "customer@example.com";

        Reservation reservation = Reservation.builder()
                .id(1L)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.of(2026, 10, 29, 10, 0))
                .build();

        ReservationResponse response = new ReservationResponse(
                1L,
                5L,
                10L,
                null,
                ReservationType.HOTEL,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 1, 15),
                reservation.getReservedAt(),
                ReservationStatus.CONFIRMED);

        when(reservationService.getReservationsByUser(email))
                .thenReturn(List.of(reservation));

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        mockMvc.perform(get("/api/reservations")
                .with(user(email).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].roomId").value(10))
                .andExpect(jsonPath("$[0].type").value("HOTEL"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(reservationService).getReservationsByUser(email);
    }

    @Test
    void shouldGetReservationById() throws Exception {

        String email = "customer@example.com";
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.of(2026, 10, 29, 10, 0))
                .build();

        ReservationResponse response = new ReservationResponse(
                reservationId,
                5L,
                10L,
                null,
                ReservationType.HOTEL,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 1, 15),
                reservation.getReservedAt(),
                ReservationStatus.CONFIRMED);

        when(reservationService.getReservationById(reservationId, email))
                .thenReturn(reservation);

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        mockMvc.perform(get("/api/reservations/{id}", reservationId)
                .with(user(email).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.roomId").value(10))
                .andExpect(jsonPath("$.type").value("HOTEL"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).getReservationById(reservationId, email);
    }

    @Test
    void shouldCancelReservation() throws Exception {

        String email = "customer@example.com";
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .type(ReservationType.HOTEL)
                .status(ReservationStatus.CANCELLED)
                .reservedAt(LocalDateTime.of(2026, 10, 29, 10, 0))
                .build();

        ReservationResponse response = new ReservationResponse(
                reservationId,
                5L,
                10L,
                null,
                ReservationType.HOTEL,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 1, 15),
                reservation.getReservedAt(),
                ReservationStatus.CANCELLED);

        when(reservationService.cancelReservation(reservationId, email))
                .thenReturn(reservation);

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId)
                .with(user(email).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.roomId").value(10))
                .andExpect(jsonPath("$.type").value("HOTEL"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(reservationService).cancelReservation(reservationId, email);
    }

}
