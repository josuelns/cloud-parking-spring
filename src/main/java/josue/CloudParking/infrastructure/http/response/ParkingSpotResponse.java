package josue.CloudParking.infrastructure.http.response;

import josue.CloudParking.domain.SpotStatus;

public record ParkingSpotResponse(
        String code,
        SpotStatus status,
        String license
) {
}
