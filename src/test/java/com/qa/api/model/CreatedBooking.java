package com.qa.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response body of POST /booking. */
public record CreatedBooking(
        @JsonProperty("bookingid") int bookingId,
        @JsonProperty("booking") Booking booking) {
}
