package josue.CloudParking.domain.service;

import josue.CloudParking.domain.ParkingSession;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class ParkingCheckOut {

    public static final int ONE_HOUR = 60;
    public static final int TWENTY_FOUR_HOUR = 24 * ONE_HOUR;
    public static final double ONE_HOUR_VALUE = 5.00;
    public static final double ADDITIONAL_PER_HOUR_VALUE = 2.00;
    public static final double DAY_VALUE = 20.00;

    private ParkingCheckOut() {
    }

    public static Double calculateBill(LocalDateTime entryDate, LocalDateTime exitDate) {
        long minutes = entryDate.until(exitDate, ChronoUnit.MINUTES);
        if (minutes <= ONE_HOUR) {
            return ONE_HOUR_VALUE;
        }
        if (minutes <= TWENTY_FOUR_HOUR) {
            double bill = ONE_HOUR_VALUE;
            int hours = (int) (minutes / ONE_HOUR);
            bill += hours * ADDITIONAL_PER_HOUR_VALUE;
            return bill;
        }
        int days = (int) (minutes / TWENTY_FOUR_HOUR);
        return days * DAY_VALUE;
    }

    public static Double calculateBill(ParkingSession session) {
        return calculateBill(session.getEntryDate(), session.getExitDate());
    }
}
