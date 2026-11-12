package com.qa.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record BookingDates(
        @JsonProperty("checkin") LocalDate checkIn,
        @JsonProperty("checkout") LocalDate checkOut) {
}
