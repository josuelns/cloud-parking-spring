package josue.CloudParking.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SessionAlreadyClosedException extends RuntimeException {

    public SessionAlreadyClosedException(String id) {
        super("Sessão já encerrada: " + id);
    }
}
