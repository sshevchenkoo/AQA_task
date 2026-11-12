package com.qa.api.data;

import com.qa.api.model.Booking;
import com.qa.api.model.BookingDates;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BookingData {

    private BookingData() {
    }

    public static Booking validBooking() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        LocalDate checkIn = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 30));

        return new Booking(
                "Yaroslav-" + uniqueSuffix,
                "Tester-" + uniqueSuffix,
                ThreadLocalRandom.current().nextInt(50, 1000),
                true,
                new BookingDates(checkIn, checkIn.plusDays(3)),
                "Breakfast");
    }
}
