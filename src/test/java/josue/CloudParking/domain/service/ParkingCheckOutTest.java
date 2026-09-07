package josue.CloudParking.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParkingCheckOutTest {

    @Test
    void shouldChargeFixedValueWithinOneHour() {
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = entry.plusMinutes(30);

        assertEquals(5.00, ParkingCheckOut.calculateBill(entry, exit));
    }

    @Test
    void shouldChargeAdditionalHoursWithinTwentyFourHours() {
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = entry.plusMinutes(150);

        assertEquals(9.00, ParkingCheckOut.calculateBill(entry, exit));
    }

    @Test
    void shouldChargeDailyValueAfterTwentyFourHours() {
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = entry.plusHours(25);

        assertEquals(20.00, ParkingCheckOut.calculateBill(entry, exit));
    }
}
