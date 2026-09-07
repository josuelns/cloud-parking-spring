package josue.CloudParking.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SpotNotAvailableException extends RuntimeException {

    public SpotNotAvailableException(String spotCode) {
        super("Vaga " + spotCode + " não está disponível");
    }
}
