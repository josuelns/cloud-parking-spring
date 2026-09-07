package josue.CloudParking.infrastructure.mapper;

import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.infrastructure.http.response.VehicleInfoResponse;
import org.mapstruct.Mapper;

@Mapper
public interface VehicleInfoMapper {

    VehicleInfoResponse toResponse(VehicleInfo vehicleInfo);
}
