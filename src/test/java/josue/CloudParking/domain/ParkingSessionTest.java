package josue.CloudParking.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingSessionTest {

    @Test
    void shouldStartAsOpenSession() {
        ParkingSession session = new ParkingSession("id1", "A1", "ABC1D23", "SP", "Uno", "Branco");

        assertTrue(session.isOpen());
        assertNotNull(session.getEntryDate());
        assertNull(session.getExitDate());
        assertNull(session.getBill());
    }

    @Test
    void shouldCloseSession() {
        ParkingSession session = new ParkingSession("id1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        LocalDateTime exitDate = LocalDateTime.of(2026, 1, 1, 12, 0);

        session.close(exitDate, 9.0);

        assertFalse(session.isOpen());
        assertEquals(exitDate, session.getExitDate());
        assertEquals(9.0, session.getBill());
    }
}
