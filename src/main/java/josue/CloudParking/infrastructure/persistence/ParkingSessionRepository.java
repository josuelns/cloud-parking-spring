package josue.CloudParking.infrastructure.persistence;

import josue.CloudParking.domain.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, String> {

    List<ParkingSession> findByExitDateIsNull();

    Optional<ParkingSession> findByLicenseAndExitDateIsNull(String license);

    Optional<ParkingSession> findBySpotCodeAndExitDateIsNull(String spotCode);
}
