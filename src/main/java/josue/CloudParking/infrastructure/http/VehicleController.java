package josue.CloudParking.infrastructure.http;

import josue.CloudParking.application.VehicleLookupUseCase;
import josue.CloudParking.infrastructure.http.response.VehicleInfoResponse;
import josue.CloudParking.infrastructure.mapper.VehicleInfoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleLookupUseCase vehicleLookupUseCase;
    private final VehicleInfoMapper vehicleInfoMapper;

    public VehicleController(VehicleLookupUseCase vehicleLookupUseCase,
                             VehicleInfoMapper vehicleInfoMapper) {
        this.vehicleLookupUseCase = vehicleLookupUseCase;
        this.vehicleInfoMapper = vehicleInfoMapper;
    }

    @GetMapping("/{plate}")
    public ResponseEntity<VehicleInfoResponse> lookupByPlate(@PathVariable String plate) {
        return vehicleLookupUseCase.lookupByPlate(plate)
                .map(vehicleInfoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
