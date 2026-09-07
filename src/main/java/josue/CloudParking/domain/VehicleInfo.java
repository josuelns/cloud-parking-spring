package josue.CloudParking.domain;

public record VehicleInfo(
        String plate,
        String brand,
        String model,
        String color,
        Integer year,
        String state
) {
}
