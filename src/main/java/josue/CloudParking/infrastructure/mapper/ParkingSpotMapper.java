package josue.CloudParking.infrastructure.mapper;

import josue.CloudParking.domain.ParkingSession;
import josue.CloudParking.domain.ParkingSpot;
import josue.CloudParking.domain.SpotStatus;
import josue.CloudParking.infrastructure.http.response.ParkingSpotResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ParkingSpotMapper {

    @Mapping(target = "code", source = "spot.code")
    @Mapping(target = "status", source = "spot.status")
    @Mapping(target = "license", source = "spot", qualifiedByName = "resolveLicense")
    ParkingSpotResponse toResponse(ParkingSpot spot, @Context Map<String, String> licenseBySpot);

    List<ParkingSpotResponse> toResponseList(List<ParkingSpot> spots, @Context Map<String, String> licenseBySpot);

    default List<ParkingSpotResponse> toResponseList(List<ParkingSpot> spots, List<ParkingSession> openSessions) {
        Map<String, String> licenseBySpot = openSessions.stream()
                .collect(Collectors.toMap(ParkingSession::getSpotCode, ParkingSession::getLicense));
        return toResponseList(spots, licenseBySpot);
    }

    @Named("resolveLicense")
    default String resolveLicense(ParkingSpot spot, @Context Map<String, String> licenseBySpot) {
        if (spot.getStatus() != SpotStatus.OCCUPIED) {
            return null;
        }
        return licenseBySpot.get(spot.getCode());
    }
}
