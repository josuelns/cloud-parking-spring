package josue.CloudParking.infrastructure.http.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import josue.CloudParking.domain.SpotStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParkingSessionResponse(
        String id,
        String spotCode,
        String license,
        String state,
        String model,
        String color,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime entryDate,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime exitDate,
        Double bill
) {
}
