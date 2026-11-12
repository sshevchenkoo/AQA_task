package com.qa.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qa.api.model.Booking;
import com.qa.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/** Client for the /booking resource. Returns raw responses; assertions belong to the tests. */
public class BookingClient {

    private static final String BOOKING = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";

    private final RequestSpecification baseSpec;

    public BookingClient() {
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.apiBaseUrl())
                .setContentType(ContentType.JSON)
                // The API answers 418 to REST Assured's default multi-value JSON Accept header.
                .addHeader("Accept", "application/json")
                .setConfig(RestAssuredConfig.config()
                        .objectMapperConfig(isoDatesMapperConfig())
                        .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .addFilter(new AllureRestAssured())
                .build();
    }

    public Response createBooking(Booking booking) {
        return createBookingFrom(booking);
    }

    /** Accepts any body, including invalid payloads. */
    public Response createBookingFrom(Object body) {
        return request().body(body).post(BOOKING);
    }

    public Response getBooking(int bookingId) {
        return request().get(BOOKING_BY_ID, bookingId);
    }

    public Response deleteBooking(int bookingId) {
        return request()
                .auth().preemptive().basic(TestConfig.apiUsername(), TestConfig.apiPassword())
                .delete(BOOKING_BY_ID, bookingId);
    }

    public Response deleteBookingWithoutAuth(int bookingId) {
        return request().delete(BOOKING_BY_ID, bookingId);
    }

    private RequestSpecification request() {
        return given().spec(baseSpec);
    }

    /** Serializes LocalDate as an ISO string ("2026-01-31"), as the API expects. */
    private static ObjectMapperConfig isoDatesMapperConfig() {
        return ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                (type, charset) -> new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }
}
