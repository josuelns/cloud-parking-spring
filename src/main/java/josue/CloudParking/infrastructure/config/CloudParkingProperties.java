package josue.CloudParking.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud-parking")
public record CloudParkingProperties(
        GridProperties grid,
        VehicleLookupProperties vehicleLookup
) {

    public record GridProperties(
            Integer totalSpots,
            Integer rows,
            Integer columns
    ) {
    }

    public record VehicleLookupProperties(
            String provider,
            ApiBrasilProperties apibrasil
    ) {
    }

    public record ApiBrasilProperties(
            String baseUrl,
            String deviceToken,
            String bearerToken
    ) {
    }
}
