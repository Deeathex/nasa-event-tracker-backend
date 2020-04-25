package NASA.model.exceptions;

import lombok.Getter;

@Getter
public class EONETException extends RuntimeException {
    private String message;

    public EONETException(String message) {
        super(message);
    }
}
