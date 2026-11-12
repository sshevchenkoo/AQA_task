package com.qa.api.tests;

import com.qa.api.client.BookingClient;
import com.qa.api.data.BookingData;
import com.qa.api.model.Booking;
import com.qa.api.model.CreatedBooking;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@DisplayName("Restful Booker: booking API")
class BookingTest {

    private final BookingClient bookingClient = new BookingClient();
    private final List<Integer> createdBookingIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedBookings() {
        createdBookingIds.forEach(bookingClient::deleteBooking);
    }

    @Test
    @DisplayName("Created booking can be retrieved by id with the same data")
    void createdBookingCanBeRetrievedById() {
        Booking expected = BookingData.validBooking();

        CreatedBooking created = bookingClient.createBooking(expected)
                .then()
                .statusCode(SC_OK) // the API returns 200, not 201, on create
                .contentType(ContentType.JSON)
                .extract().as(CreatedBooking.class);
        createdBookingIds.add(created.bookingId());

        assertThat(created.bookingId()).as("generated booking id").isPositive();
        assertThat(created.booking()).as("booking echoed by POST /booking")
                .usingRecursiveComparison().isEqualTo(expected);

        Booking retrieved = bookingClient.getBooking(created.bookingId())
                .then()
                .statusCode(SC_OK)
                .contentType(ContentType.JSON)
                .extract().as(Booking.class);

        assertThat(retrieved).as("booking returned by GET /booking/%d", created.bookingId())
                .usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Deleted booking is no longer available: GET returns 404")
    void deletedBookingIsNotFound() {
        int bookingId = createBooking();

        // the API returns 201 on a successful delete
        bookingClient.deleteBooking(bookingId).then().statusCode(SC_CREATED);

        bookingClient.getBooking(bookingId).then().statusCode(SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Booking cannot be deleted without authorization: 403, booking stays intact")
    void bookingCannotBeDeletedWithoutAuthorization() {
        int bookingId = createBooking();

        bookingClient.deleteBookingWithoutAuth(bookingId).then().statusCode(SC_FORBIDDEN);

        bookingClient.getBooking(bookingId).then().statusCode(SC_OK);
    }

    @Test
    @DisplayName("Booking without mandatory fields is rejected")
    void bookingWithoutMandatoryFieldsIsRejected() {
        Map<String, Object> incompleteBooking = Map.of("firstname", "OnlyFirstName");

        // the API returns 500, not 400, on validation errors
        bookingClient.createBookingFrom(incompleteBooking).then().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    private int createBooking() {
        int bookingId = bookingClient.createBooking(BookingData.validBooking())
                .then()
                .statusCode(SC_OK)
                .extract().as(CreatedBooking.class)
                .bookingId();
        createdBookingIds.add(bookingId);
        return bookingId;
    }
}
