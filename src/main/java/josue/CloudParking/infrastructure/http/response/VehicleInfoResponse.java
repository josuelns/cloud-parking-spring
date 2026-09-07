package josue.CloudParking.infrastructure.http.response;

public record VehicleInfoResponse(
        String plate,
        String brand,
        String model,
        String color,
        Integer year,
        String state
) {
}
