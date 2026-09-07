package josue.CloudParking.infrastructure.mapper;

import josue.CloudParking.domain.ParkingSession;
import josue.CloudParking.infrastructure.http.response.ParkingSessionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ParkingSessionMapper {

    ParkingSessionResponse toResponse(ParkingSession session);

    List<ParkingSessionResponse> toResponseList(List<ParkingSession> sessions);
}
