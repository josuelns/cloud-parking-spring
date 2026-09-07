package josue.CloudParking.infrastructure.http;

import josue.CloudParking.application.ParkingOperationsUseCase;
import josue.CloudParking.infrastructure.http.request.CheckInRequest;
import josue.CloudParking.infrastructure.http.response.ParkingSessionResponse;
import josue.CloudParking.infrastructure.mapper.ParkingSessionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parking")
public class ParkingController {

    private final ParkingOperationsUseCase parkingOperationsUseCase;
    private final ParkingSessionMapper parkingSessionMapper;

    public ParkingController(ParkingOperationsUseCase parkingOperationsUseCase,
                             ParkingSessionMapper parkingSessionMapper) {
        this.parkingOperationsUseCase = parkingOperationsUseCase;
        this.parkingSessionMapper = parkingSessionMapper;
    }

    @GetMapping
    public List<ParkingSessionResponse> listOpenSessions() {
        return parkingSessionMapper.toResponseList(parkingOperationsUseCase.listOpenSessions());
    }

    @GetMapping("/{id}")
    public ParkingSessionResponse findById(@PathVariable String id) {
        return parkingSessionMapper.toResponse(parkingOperationsUseCase.findSessionById(id));
    }

    @PostMapping
    public ResponseEntity<ParkingSessionResponse> checkIn(@RequestBody CheckInRequest request) {
        var session = parkingOperationsUseCase.checkIn(request.plate(), request.spotCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSessionMapper.toResponse(session));
    }

    @PostMapping("/{id}/exit")
    public ParkingSessionResponse checkOut(@PathVariable String id) {
        return parkingSessionMapper.toResponse(parkingOperationsUseCase.checkOut(id));
    }
}
