package josue.CloudParking.infrastructure.http.request;

public record CheckInRequest(
        String plate,
        String spotCode
) {
}
