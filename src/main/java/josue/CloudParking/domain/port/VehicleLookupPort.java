package josue.CloudParking.domain.port;

import josue.CloudParking.domain.VehicleInfo;

import java.util.Optional;

public interface VehicleLookupPort {

    Optional<VehicleInfo> lookupByPlate(String plate);
}
