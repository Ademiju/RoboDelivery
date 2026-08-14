package miju.com.robodelivery.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class APIErrorException extends RuntimeException {

    private String message;
    private HttpStatus httpStatus;
    private String statusCode;
    private String timestamp;
}
