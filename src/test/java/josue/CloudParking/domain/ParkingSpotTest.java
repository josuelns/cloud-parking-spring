package josue.CloudParking.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingSpotTest {

    @Test
    void shouldStartAsFree() {
        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);

        assertTrue(spot.isFree());
        assertEquals(SpotStatus.FREE, spot.getStatus());
    }

    @Test
    void shouldOccupyAndReleaseSpot() {
        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);

        spot.occupy();
        assertFalse(spot.isFree());
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());

        spot.release();
        assertTrue(spot.isFree());
        assertEquals(SpotStatus.FREE, spot.getStatus());
    }
}
