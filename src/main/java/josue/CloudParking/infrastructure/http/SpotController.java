package josue.CloudParking.infrastructure.http;

import josue.CloudParking.application.ParkingOperationsUseCase;
import josue.CloudParking.infrastructure.http.response.ParkingSpotResponse;
import josue.CloudParking.infrastructure.http.response.SpotMapResponse;
import josue.CloudParking.infrastructure.mapper.ParkingSpotMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spots")
public class SpotController {

    private final ParkingOperationsUseCase parkingOperationsUseCase;
    private final ParkingSpotMapper parkingSpotMapper;

    public SpotController(ParkingOperationsUseCase parkingOperationsUseCase,
                          ParkingSpotMapper parkingSpotMapper) {
        this.parkingOperationsUseCase = parkingOperationsUseCase;
        this.parkingSpotMapper = parkingSpotMapper;
    }

    @GetMapping
    public SpotMapResponse getSpotMap() {
        var dimensions = parkingOperationsUseCase.getGridDimensions();
        var spots = parkingOperationsUseCase.listAllSpots();
        var openSessions = parkingOperationsUseCase.listOpenSessions();
        return new SpotMapResponse(
                dimensions.rows(),
                dimensions.columns(),
                parkingSpotMapper.toResponseList(spots, openSessions)
        );
    }

    @GetMapping("/available")
    public List<ParkingSpotResponse> getAvailableSpots() {
        return parkingSpotMapper.toResponseList(
                parkingOperationsUseCase.listAvailableSpots(),
                Map.of()
        );
    }
}
