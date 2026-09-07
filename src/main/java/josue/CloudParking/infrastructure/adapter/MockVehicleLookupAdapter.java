package josue.CloudParking.infrastructure.adapter;

import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.domain.port.VehicleLookupPort;
import josue.CloudParking.infrastructure.util.PlateNormalizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "cloud-parking.vehicle-lookup.provider", havingValue = "mock", matchIfMissing = true)
public class MockVehicleLookupAdapter implements VehicleLookupPort {

    private static final Map<String, VehicleInfo> MOCK_DATA = Map.of(
            "ABC1D23", new VehicleInfo("ABC1D23", "Fiat", "Uno", "Branco", 2018, "SP"),
            "XYZ9K88", new VehicleInfo("XYZ9K88", "Volkswagen", "Gol", "Prata", 2020, "RJ"),
            "DEF4G56", new VehicleInfo("DEF4G56", "Chevrolet", "Onix", "Preto", 2022, "MG")
    );

    @Override
    public Optional<VehicleInfo> lookupByPlate(String plate) {
        String normalized = PlateNormalizer.normalize(plate);
        VehicleInfo known = MOCK_DATA.get(normalized);
        if (known != null) {
            return Optional.of(known);
        }
        return Optional.of(new VehicleInfo(
                normalized,
                "Genérico",
                "Veículo",
                "Indefinida",
                null,
                "SP"
        ));
    }
}
