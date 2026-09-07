package josue.CloudParking.infrastructure.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.domain.port.VehicleLookupPort;
import josue.CloudParking.infrastructure.config.CloudParkingProperties;
import josue.CloudParking.infrastructure.util.PlateNormalizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "cloud-parking.vehicle-lookup.provider", havingValue = "apibrasil")
public class ApiBrasilVehicleLookupAdapter implements VehicleLookupPort {

    private final RestClient restClient;
    private final CloudParkingProperties.ApiBrasilProperties config;

    public ApiBrasilVehicleLookupAdapter(CloudParkingProperties properties) {
        this.config = properties.vehicleLookup().apibrasil();
        this.restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .build();
    }

    @Override
    public Optional<VehicleInfo> lookupByPlate(String plate) {
        String normalized = PlateNormalizer.normalize(plate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("DeviceToken", config.deviceToken());
        headers.setBearerAuth(config.bearerToken());

        try {
            ApiBrasilResponse response = restClient.post()
                    .uri("/api/v2/vehicles/dados")
                    .headers(h -> {
                        h.addAll(headers);
                    })
                    .body(Map.of("placa", normalized))
                    .retrieve()
                    .body(ApiBrasilResponse.class);

            if (response == null || response.marca() == null) {
                return Optional.empty();
            }

            return Optional.of(new VehicleInfo(
                    normalized,
                    response.marca(),
                    response.modelo(),
                    response.cor(),
                    parseYear(response.ano()),
                    response.uf()
            ));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    private Integer parseYear(String year) {
        if (year == null || year.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(year.replaceAll("\\D", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiBrasilResponse(
            String marca,
            String modelo,
            String cor,
            String ano,
            String uf
    ) {
    }
}
