package josue.CloudParking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_spots")
public class ParkingSpot {

    @Id
    private String code;

    private char rowLetter;

    private int columnNumber;

    @Enumerated(EnumType.STRING)
    private SpotStatus status;

    protected ParkingSpot() {
    }

    public ParkingSpot(String code, char rowLetter, int columnNumber) {
        this.code = code;
        this.rowLetter = rowLetter;
        this.columnNumber = columnNumber;
        this.status = SpotStatus.FREE;
    }

    public String getCode() {
        return code;
    }

    public char getRowLetter() {
        return rowLetter;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void occupy() {
        this.status = SpotStatus.OCCUPIED;
    }

    public void release() {
        this.status = SpotStatus.FREE;
    }

    public boolean isFree() {
        return status == SpotStatus.FREE;
    }
}
