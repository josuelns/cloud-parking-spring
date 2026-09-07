package josue.CloudParking.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateOperationException extends RuntimeException {

    public DuplicateOperationException(String message) {
        super(message);
    }
}
