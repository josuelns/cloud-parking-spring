package josue.CloudParking.application;

import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.domain.port.VehicleLookupPort;
import josue.CloudParking.infrastructure.util.PlateNormalizer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleLookupUseCase {

    private final VehicleLookupPort vehicleLookupPort;

    public VehicleLookupUseCase(VehicleLookupPort vehicleLookupPort) {
        this.vehicleLookupPort = vehicleLookupPort;
    }

    public Optional<VehicleInfo> lookupByPlate(String plate) {
        return vehicleLookupPort.lookupByPlate(PlateNormalizer.normalize(plate));
    }
}
