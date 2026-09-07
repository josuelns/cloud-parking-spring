package josue.CloudParking.infrastructure.config;

import org.springframework.stereotype.Component;

@Component
public class ParkingGridCalculator {

    public GridDimensions resolve(CloudParkingProperties.GridProperties grid) {
        if (grid.rows() != null && grid.columns() != null) {
            return new GridDimensions(grid.rows(), grid.columns());
        }

        int totalSpots = grid.totalSpots() != null ? grid.totalSpots() : 20;
        return computeFromTotal(totalSpots);
    }

    public GridDimensions computeFromTotal(int totalSpots) {
        int bestRows = 1;
        int bestCols = totalSpots;
        int minArea = totalSpots;

        for (int rows = 1; rows <= 26; rows++) {
            int cols = (int) Math.ceil((double) totalSpots / rows);
            int area = rows * cols;
            if (area < totalSpots) {
                continue;
            }
            if (area < minArea || (area == minArea && Math.abs(rows - cols) < Math.abs(bestRows - bestCols))) {
                minArea = area;
                bestRows = rows;
                bestCols = cols;
            }
        }

        return new GridDimensions(bestRows, bestCols);
    }

    public record GridDimensions(int rows, int columns) {
    }
}
