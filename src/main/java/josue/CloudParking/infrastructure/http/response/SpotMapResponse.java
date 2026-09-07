package josue.CloudParking.infrastructure.http.response;

import java.util.List;

public record SpotMapResponse(
        int rows,
        int columns,
        List<ParkingSpotResponse> spots
) {
}
