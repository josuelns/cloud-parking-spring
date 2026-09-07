package josue.CloudParking.infrastructure.config;

import josue.CloudParking.domain.ParkingSpot;
import josue.CloudParking.infrastructure.persistence.ParkingSpotRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ParkingSpotInitializer implements ApplicationRunner {

    private final CloudParkingProperties properties;
    private final ParkingGridCalculator gridCalculator;
    private final ParkingSpotRepository spotRepository;

    public ParkingSpotInitializer(CloudParkingProperties properties,
                                    ParkingGridCalculator gridCalculator,
                                    ParkingSpotRepository spotRepository) {
        this.properties = properties;
        this.gridCalculator = gridCalculator;
        this.spotRepository = spotRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        var dimensions = gridCalculator.resolve(properties.grid());
        int totalSpots = properties.grid().totalSpots() != null
                ? properties.grid().totalSpots()
                : dimensions.rows() * dimensions.columns();

        int created = 0;
        for (int row = 0; row < dimensions.rows() && created < totalSpots; row++) {
            char rowLetter = (char) ('A' + row);
            for (int col = 1; col <= dimensions.columns() && created < totalSpots; col++) {
                String code = rowLetter + String.valueOf(col);
                if (spotRepository.existsById(code)) {
                    created++;
                    continue;
                }
                spotRepository.save(new ParkingSpot(code, rowLetter, col));
                created++;
            }
        }
    }
}
