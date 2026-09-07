package josue.CloudParking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_sessions")
public class ParkingSession {

    @Id
    private String id;

    private String spotCode;

    private String license;

    private String state;

    private String model;

    private String color;

    private LocalDateTime entryDate;

    private LocalDateTime exitDate;

    private Double bill;

    protected ParkingSession() {
    }

    public ParkingSession(String id, String spotCode, String license, String state, String model, String color) {
        this.id = id;
        this.spotCode = spotCode;
        this.license = license;
        this.state = state;
        this.model = model;
        this.color = color;
        this.entryDate = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public String getLicense() {
        return license;
    }

    public String getState() {
        return state;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public LocalDateTime getExitDate() {
        return exitDate;
    }

    public Double getBill() {
        return bill;
    }

    public boolean isOpen() {
        return exitDate == null;
    }

    public void close(LocalDateTime exitDate, Double bill) {
        this.exitDate = exitDate;
        this.bill = bill;
    }
}
