package josue.CloudParking.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParkingGridCalculatorTest {

    private final ParkingGridCalculator calculator = new ParkingGridCalculator();

    @Test
    void shouldComputeGridForTwentySpots() {
        var dimensions = calculator.computeFromTotal(20);

        assertEquals(4, dimensions.rows());
        assertEquals(5, dimensions.columns());
    }

    @Test
    void shouldComputeGridForTenSpots() {
        var dimensions = calculator.computeFromTotal(10);

        assertEquals(2, dimensions.rows());
        assertEquals(5, dimensions.columns());
    }
}
