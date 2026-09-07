package josue.CloudParking.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import josue.CloudParking.domain.ParkingSpot;
import josue.CloudParking.domain.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, String> {

    List<ParkingSpot> findByStatus(SpotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSpot s WHERE s.code = :code")
    Optional<ParkingSpot> findByCodeForUpdate(@Param("code") String code);
}
